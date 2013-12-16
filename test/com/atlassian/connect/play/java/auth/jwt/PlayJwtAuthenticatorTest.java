package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.jwt.Jwt;
import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.core.http.auth.JwtAuthenticator;
import com.atlassian.jwt.core.reader.JwtIssuerSharedSecretService;
import com.atlassian.jwt.core.reader.JwtIssuerValidator;
import com.atlassian.jwt.core.reader.NimbusJwtReaderFactory;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilderFactory;
import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.atlassian.jwt.reader.JwtReaderFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import play.libs.F;
import play.mvc.Results;

import java.util.concurrent.TimeUnit;

import static com.atlassian.jwt.JwtConstants.JWT_PARAM_NAME;
import static com.atlassian.jwt.core.http.JwtHttpConstants.AUTHORIZATION_HEADER;
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

    @Mock
    private JwtIssuerSharedSecretService jwtIssuerSharedSecretService;

    @Mock
    private JwtIssuerValidator jwtIssuerValidator;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Request request;

    private JwtAuthenticator<Request, Response, JwtAuthenticationResult> jwtAuthenticator;

    private JsonSmartJwtJsonBuilderFactory builderFactory = new JsonSmartJwtJsonBuilderFactory();
    private NimbusJwtWriterFactory writerFactory = new NimbusJwtWriterFactory();

    private String createJwt() {
        long now = System.currentTimeMillis();
        String json = builderFactory.jsonBuilder()
                .issuer("joe")
                .issuedAt(now)
                .expirationTime(now + TimeUnit.MINUTES.toSeconds(10))
                .subject("fred")
                .build();
        return writerFactory.macSigningWriter(ALGORITHM, PASSWORD).jsonToJwt(json);
    }
    @Before
    public void init() {
        JwtReaderFactory readerFactory = new NimbusJwtReaderFactory(jwtIssuerValidator, jwtIssuerSharedSecretService);
        jwtAuthenticator = new PlayJwtAuthenticator(new PlayJwtRequestExtractor(), new PlayAuthenticationResultHandler(),
                readerFactory);
    }

    private Either<Status, Jwt> authenticate(String parameterJwt, String headerJwt) throws JwtUnknownIssuerException, JwtIssuerLacksSharedSecretException {
        when(request.getQueryString(anyString())).thenReturn(parameterJwt);
        when(request.headers().get(anyString())).thenReturn(headerJwt == null ? new String[]{} : new String[]{headerJwt});
        when(jwtIssuerValidator.isValid(anyString())).thenReturn(true);
        when(jwtIssuerSharedSecretService.getSharedSecret(anyString())).thenReturn(PASSWORD);
        Response response = new Response();
        return jwtAuthenticator.authenticate(request, response).getResult();
    }

    @Test
    public void looksInRequestParamsForJwt() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException {
        authenticate(null, null);
        verify(request).getQueryString(JWT_PARAM_NAME);
    }

    @Test
    public void looksInHeaderForJwtWhenNotInParams() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException {
        authenticate(null, null);
        verify(request.headers()).get(AUTHORIZATION_HEADER);
    }

    @Test
    public void returnsErrorWhenNoJwtInRequest() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException {
        assertThat(authenticate(null, null).left.isDefined(), equalTo(true));
    }

    @Test
    public void returnsStatusInternalServerErrorWhenNoJwtInRequest() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException {
        assertThat(authenticate(null, null).left.get().getWrappedSimpleResult().header().status(), equalTo(500));
    }

    @Test
    public void validatesWithIssueValidator() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException {
        String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJqb2UiLAogImV4cCI6MTMwMDgxOTM4MCwKICJodHRwOi8vZXhhbXBsZS5jb20vaXNfcm9vdCI6dHJ1ZX0.FiSys799P0mmChbQXoj76wsXrjnPP7HDlIW76orDjV8";

        Either<Status, Jwt> result = authenticate(createJwt(), null);
        verify(jwtIssuerValidator).isValid("joe");
//        assertThat(result.right.isDefined(), equalTo(true));
    }

    @Test
    public void blah() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException {
        Either<Status, Jwt> result = authenticate(createJwt(), null);
        assertThat(result.right.isDefined(), equalTo(true));
    }
}
