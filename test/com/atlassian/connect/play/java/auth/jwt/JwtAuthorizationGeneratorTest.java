package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.http.HttpMethod;
import com.atlassian.connect.play.java.model.AcHostModel;
import com.atlassian.fugue.Option;
import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.atlassian.jwt.writer.JwtWriter;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.ObjectUtils;
import org.hamcrest.Matcher;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.libs.Json;
import play.test.FakeApplication;
import play.test.Helpers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.atlassian.jwt.JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JwtAuthorizationGeneratorTest {

    private static final Option<String> FREDDY = Option.some("freddy");
    private static final String NOT_SO_SECRET_SECRET = "notSoSecretSecret";
    private static final String MY_PLUGIN_KEY = "myPluginKey";
    private static final String PRODUCT_CONTEXT = "/pc";

    @Mock
    private JwtWriterFactory jwtWriterFactory;// = new NimbusJwtWriterFactory();

    @Mock
    private JwtWriter jwtWriter;

    private JwtAuthorizationGenerator jwtAuthorizationGenerator;

    private AcHostModel acHost;
    private String aUrl;

    @Before
    public void init() throws URISyntaxException {
        jwtAuthorizationGenerator = new JwtAuthorizationGenerator(jwtWriterFactory, 60 * 3);
        aUrl = PRODUCT_CONTEXT + "/foo";
        FakeApplication fakeApplication = Helpers.fakeApplication();
        Helpers.start(fakeApplication);
        acHost = new AcHostModel();
        acHost.sharedSecret = NOT_SO_SECRET_SECRET;

        when(jwtWriterFactory.macSigningWriter(any(SigningAlgorithm.class), anyString())).thenReturn(jwtWriter);

        AC.PLUGIN_KEY = MY_PLUGIN_KEY;
    }

    @Test
    public void startsWithJwtAuthPrefix() throws URISyntaxException, JwtUnknownIssuerException, JwtIssuerLacksSharedSecretException {
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

    // TODO: tests for exp and iat

    private Option<String> generate() throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, URISyntaxException {
        return jwtAuthorizationGenerator.generate("GET", aUrl, ImmutableMap.<String, List<String>>of(), acHost, FREDDY);
    }

    private Matcher<String> isJwtWithIssValue(String issuer) {
        return isJwtWithStringFieldValue("iss", issuer);
    }

    private Matcher<String> isJwtWithSubValue(String subject) {
        return isJwtWithStringFieldValue("sub", subject);
    }

    private Matcher<String> isJwtWithStringFieldValue(String fieldName, String fieldValue) {
        return new JwtJsonMatcher<String>(fieldName, fieldValue) {
            @Override
            String getValue(JsonNode node) {
                return node.textValue();
            }
        };
    }

    private abstract class JwtJsonMatcher<T> extends ArgumentMatcher<String> {
        private final String fieldName;
        private final T fieldValue;

        public JwtJsonMatcher(String fieldName, T fieldValue) {
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
        }

        @Override
        public boolean matches(Object argument) {
            System.out.println(argument);
            JsonNode jwtJson = Json.parse(argument.toString());

            JsonNode jwtFieldJson = jwtJson.get(fieldName);
            if (jwtFieldJson == null)
                return fieldValue == null;

            T value = getValue(jwtFieldJson);
            return ObjectUtils.equals(value, fieldValue);
        }

        abstract T getValue(JsonNode node);
    }


}
