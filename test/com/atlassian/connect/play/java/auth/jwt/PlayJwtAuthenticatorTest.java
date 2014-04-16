package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.jwt.CanonicalHttpRequest;
import com.atlassian.jwt.Jwt;
import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.core.HttpRequestCanonicalizer;
import com.atlassian.jwt.core.http.auth.JwtAuthenticator;
import com.atlassian.jwt.core.reader.JwtIssuerSharedSecretService;
import com.atlassian.jwt.core.reader.JwtIssuerValidator;
import com.atlassian.jwt.core.reader.NimbusJwtReaderFactory;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilderFactory;
import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.reader.JwtReaderFactory;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import play.libs.F;
import play.mvc.Results;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static com.atlassian.connect.play.java.auth.jwt.PlayJwtRequestExtractor.AddonContextProvider;
import static com.atlassian.jwt.JwtConstants.JWT_PARAM_NAME;
import static com.atlassian.jwt.JwtConstants.HttpRequests.AUTHORIZATION_HEADER;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static play.libs.F.Either;
import static play.mvc.Http.Request;
import static play.mvc.Http.Response;
import static play.mvc.Results.Status;

@RunWith(MockitoJUnitRunner.class)
public class PlayJwtAuthenticatorTest {
    private static final String PASSWORD = "secret";
    private static final SigningAlgorithm ALGORITHM = SigningAlgorithm.HS256;
    private static final String ISSUER = "joe";
    private static final String METHOD = "GET";
    private static final String PATH = "/foo";
    private static final String SUBJECT = "fred";

    @Mock
    private JwtIssuerSharedSecretService jwtIssuerSharedSecretService;

    @Mock
    private JwtIssuerValidator jwtIssuerValidator;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Request request;

    @Mock private AddonContextProvider contextProvider;

    private JwtAuthenticator<Request, Response, JwtAuthenticationResult> jwtAuthenticator;

    private JsonSmartJwtJsonBuilderFactory builderFactory = new JsonSmartJwtJsonBuilderFactory();
    private NimbusJwtWriterFactory writerFactory = new NimbusJwtWriterFactory();

    private String createJwt() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        CanonicalHttpRequest canonicalHttpRequest  = new CanonicalHttpUriRequest(METHOD, PATH, "/");
        String hash = HttpRequestCanonicalizer.computeCanonicalRequestHash(canonicalHttpRequest);
        long now = System.currentTimeMillis();
        String json = builderFactory.jsonBuilder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expirationTime(now + TimeUnit.MINUTES.toSeconds(10))
                .subject(SUBJECT)
                .queryHash(hash)
                .build();
        return writerFactory.macSigningWriter(ALGORITHM, PASSWORD).jsonToJwt(json);
    }

    @Before
    public void init() {
        JwtReaderFactory readerFactory = new NimbusJwtReaderFactory(jwtIssuerValidator, jwtIssuerSharedSecretService);
        jwtAuthenticator = new PlayJwtAuthenticator(new PlayJwtRequestExtractor(contextProvider),
                new PlayAuthenticationResultHandler(), readerFactory);
    }

    private Either<Status, Jwt> authenticate(String parameterJwt, String headerJwt, String addonContext)
            throws JwtUnknownIssuerException, JwtIssuerLacksSharedSecretException {
        when(contextProvider.get()).thenReturn(addonContext);
        when(request.getQueryString(anyString())).thenReturn(parameterJwt);
        when(request.headers().get(anyString())).thenReturn(headerJwt == null ? new String[]{} : new String[]{headerJwt});
        when(request.method()).thenReturn(METHOD);
        when(request.path()).thenReturn(PATH);
        when(request.queryString()).thenReturn(ImmutableMap.<String, String[]>of());
        when(jwtIssuerValidator.isValid(anyString())).thenReturn(true);
        when(jwtIssuerSharedSecretService.getSharedSecret(anyString())).thenReturn(PASSWORD);
        Response response = new Response();
        return jwtAuthenticator.authenticate(request, response).getResult();
    }

    @Test
    public void looksInRequestParamsForJwt() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException {
        authenticate(null, null, "/");
        verify(request).getQueryString(JWT_PARAM_NAME);
    }

    @Test
    public void looksInHeaderForJwtWhenNotInParams() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException {
        authenticate(null, null, "/");
        verify(request.headers()).get(AUTHORIZATION_HEADER);
    }

    @Test
    public void returnsErrorWhenNoJwtInRequest() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException {
        assertThat(authenticate(null, null, "/").left.isDefined(), equalTo(true));
    }

    @Test
    public void returnsStatusInternalServerErrorWhenNoJwtInRequest() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException {
        assertThat(authenticate(null, null, "/").left.get().getWrappedSimpleResult().header().status(), equalTo(500));
    }

    @Test
    public void validatesWithIssueValidator() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, UnsupportedEncodingException, NoSuchAlgorithmException {
        authenticate(createJwt(), null, "/");
        verify(jwtIssuerValidator).isValid(ISSUER);
    }

    @Test
    public void containsJwtWhenValidationSucceeds() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, UnsupportedEncodingException, NoSuchAlgorithmException {
        assertThat(authenticate(createJwt(), null, "/").right.isDefined(), equalTo(true));
    }

    @Test
    public void jwtContainsCorrectIssuer() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, UnsupportedEncodingException, NoSuchAlgorithmException {
        assertThat(authenticate(createJwt(), null, "/").right.get().getIssuer(), equalTo(ISSUER));
    }

    @Test
    public void jwtContainsCorrectSubject() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, UnsupportedEncodingException, NoSuchAlgorithmException {
        assertThat(authenticate(createJwt(), null, "/").right.get().getSubject(), equalTo(SUBJECT));
    }
}
