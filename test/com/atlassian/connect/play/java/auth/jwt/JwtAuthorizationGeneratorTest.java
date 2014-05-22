package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.model.AcHostModel;
import com.atlassian.fugue.Option;
import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.core.TimeUtil;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.atlassian.jwt.writer.JwtWriter;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.libs.Json;
import play.test.FakeApplication;
import play.test.Helpers;

import java.net.URISyntaxException;
import java.util.List;

import static com.atlassian.jwt.JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JwtAuthorizationGeneratorTest {

    private static final Option<String> FREDDY = Option.some("freddy");
    private static final String NOT_SO_SECRET_SECRET = "notSoSecretSecret";
    private static final String MY_PLUGIN_KEY = "myPluginKey";
    private static final String PRODUCT_CONTEXT = "/pc";
    private static final String HOST = "http://somehost:3421";
    private static final String BASE_URL = HOST + PRODUCT_CONTEXT;


    @Mock
    private JwtWriterFactory jwtWriterFactory;// = new NimbusJwtWriterFactory();

    @Mock
    private JwtWriter jwtWriter;

    private JwtAuthorizationGenerator jwtAuthorizationGenerator;

    private AcHostModel acHost;
    private String aUrl;

    @Before
    public void init() throws URISyntaxException {
        init(BASE_URL);
    }

    private void init(String baseUrl) throws URISyntaxException {
        jwtAuthorizationGenerator = new JwtAuthorizationGenerator(jwtWriterFactory, 60 * 3);
        aUrl = baseUrl + "/foo";
        FakeApplication fakeApplication = Helpers.fakeApplication();
        Helpers.start(fakeApplication);
        acHost = new AcHostModel();
        acHost.setSharedSecret(NOT_SO_SECRET_SECRET);
        acHost.setBaseUrl(baseUrl);

        when(jwtWriterFactory.macSigningWriter(any(SigningAlgorithm.class), anyString())).thenReturn(jwtWriter);

        AC.PLUGIN_KEY = MY_PLUGIN_KEY;
    }

    @Test
    public void startsWithJwtAuthPrefix() throws URISyntaxException, JwtUnknownIssuerException, JwtIssuerLacksSharedSecretException {
        assertThat(generate().get(), startsWith(JWT_AUTH_HEADER_PREFIX));
    }

    @Test
    public void worksWithRelativePath() throws URISyntaxException, JwtUnknownIssuerException, JwtIssuerLacksSharedSecretException {
        aUrl = PRODUCT_CONTEXT + "/foo";
        assertThat(generate().get(), startsWith(JWT_AUTH_HEADER_PREFIX));
    }

    @Test
    public void callsWriterWithCorrectIssuer() throws JwtUnknownIssuerException, JwtIssuerLacksSharedSecretException, URISyntaxException {
        generate();
        verify(jwtWriter).jsonToJwt(argThat(isJwtWithIssValue(MY_PLUGIN_KEY)));
    }

    @Test
    public void callsWriterWithCorrectSubject() throws JwtUnknownIssuerException, JwtIssuerLacksSharedSecretException, URISyntaxException {
        generate();
        verify(jwtWriter).jsonToJwt(argThat(isJwtWithSubValue(FREDDY.get())));
    }

    @Test
    public void callsWriterWithIatCloseToNow() throws JwtUnknownIssuerException, URISyntaxException, JwtIssuerLacksSharedSecretException {
        generate();
        verify(jwtWriter).jsonToJwt(argThat(isJwtWithIatValueCloseToNow()));
    }

    @Test
    public void callsWriterWithExpValueGreaterThanOrEqualToNow() throws JwtUnknownIssuerException, URISyntaxException, JwtIssuerLacksSharedSecretException {
        generate();
        verify(jwtWriter).jsonToJwt(argThat(isJwtWithExpValueGreaterThanOrEqualToNow()));
    }

    @Test
    public void callsWriterWithCorrectQueryHash() throws JwtUnknownIssuerException, JwtIssuerLacksSharedSecretException, URISyntaxException {
        generate();
        verify(jwtWriter).jsonToJwt(argThat(isJwtWithStringFieldValue("qsh", "dc884e24fe0f4113b128fd19b1426d7d841b6fabc03e79c2d4f27774964a5935")));
    }

    @Test
    public void worksWithEmptyProductContext() throws JwtUnknownIssuerException, JwtIssuerLacksSharedSecretException, URISyntaxException {
        init(HOST);
        generate();
        verify(jwtWriter).jsonToJwt(argThat(isJwtWithStringFieldValue("qsh", "dc884e24fe0f4113b128fd19b1426d7d841b6fabc03e79c2d4f27774964a5935")));
    }

    @Test
    public void worksWithSingleSlashProductContext() throws JwtUnknownIssuerException, JwtIssuerLacksSharedSecretException, URISyntaxException {
        init(HOST + "/");
        generate();
        verify(jwtWriter).jsonToJwt(argThat(isJwtWithStringFieldValue("qsh", "dc884e24fe0f4113b128fd19b1426d7d841b6fabc03e79c2d4f27774964a5935")));
    }

    private Option<String> generate() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, URISyntaxException {
        return jwtAuthorizationGenerator.generate("GET", aUrl, ImmutableMap.<String, List<String>>of(), acHost, FREDDY);
    }

    private Matcher<String> isJwtWithIssValue(String issuer) {
        return isJwtWithStringFieldValue("iss", issuer);
    }

    private Matcher<String> isJwtWithSubValue(String subject) {
        return isJwtWithStringFieldValue("sub", subject);
    }

    private Matcher<String> isJwtWithIatValueCloseToNow() {
        return isJwtWithLongValueMatching("iat", lessThanOrEqualTo(TimeUtil.currentTimeSeconds()));
    }

    private Matcher<String> isJwtWithExpValueGreaterThanOrEqualToNow() {
        return isJwtWithLongValueMatching("exp", greaterThanOrEqualTo(TimeUtil.currentTimeSeconds()));
    }

    private Matcher<String> isJwtWithStringFieldValue(String fieldName, String fieldValue) {
        return new JwtJsonMatcher<String>(fieldName, fieldValue) {
            @Override
            String getValue(JsonNode node) {
                return node.textValue();
            }
        };
    }

    private Matcher<String> isJwtWithLongValueMatching(String fieldName, Matcher<Long> fieldMatcher) {
        return new JwtJsonMatcher<Long>(fieldName, fieldMatcher) {
            @Override
            Long getValue(JsonNode node) {
                return node.longValue();
            }
        };
    }

    private abstract class JwtJsonMatcher<T> extends ArgumentMatcher<String> {
        private final String fieldName;
        private final Matcher<T> fieldMatcher;

        public JwtJsonMatcher(String fieldName, T fieldValue) {
            this(fieldName, equalTo(fieldValue));
        }

        public JwtJsonMatcher(String fieldName, Matcher<T> fieldMatcher) {
            this.fieldName = fieldName;
            this.fieldMatcher = fieldMatcher;
        }

        @Override
        public boolean matches(Object argument) {
            JsonNode jwtJson = Json.parse(argument.toString());

            JsonNode jwtFieldJson = jwtJson.get(fieldName);
            T value = jwtFieldJson == null ? null : getValue(jwtFieldJson);
            return fieldMatcher.matches(value);
        }

        abstract T getValue(JsonNode node);
    }


}
