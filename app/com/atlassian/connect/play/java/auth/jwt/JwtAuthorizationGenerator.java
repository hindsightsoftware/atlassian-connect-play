package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.http.HttpMethod;
import com.atlassian.connect.play.java.util.Utils;
import com.atlassian.fugue.Option;
import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.core.TimeUtil;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilder;
import com.atlassian.jwt.core.writer.JwtClaimsBuilder;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtSigningException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.httpclient.CanonicalRequestUtil;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.jwt.writer.JwtWriter;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.CharArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Play;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static com.atlassian.jwt.JwtConstants.HttpRequests.JWT_AUTH_HEADER_PREFIX;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Set the system property {@link JwtAuthorizationGenerator#JWT_EXPIRY_SECONDS_PROPERTY} with an integer value to control the size of the expiry window
 * (default is {@link JwtAuthorizationGenerator#JWT_EXPIRY_WINDOW_SECONDS_DEFAULT}).
 */
public class JwtAuthorizationGenerator {
    private static final char[] QUERY_DELIMITERS = new char[]{'&'};

    private static final String JWT_EXPIRY_SECONDS_PROPERTY = "com.atlassian.connect.jwt.expiry_seconds";
    /**
     * Default of 3 minutes.
     */
    private static final int JWT_EXPIRY_WINDOW_SECONDS_DEFAULT = 60 * 3;
    private final int jwtExpiryWindowSeconds;

    private final JwtWriterFactory jwtWriterFactory;
    private static final play.Logger.ALogger LOG = Utils.LOGGER;

    public JwtAuthorizationGenerator(JwtWriterFactory jwtWriterFactory) {
        this(jwtWriterFactory, Play.application().configuration().getInt(JWT_EXPIRY_SECONDS_PROPERTY, JWT_EXPIRY_WINDOW_SECONDS_DEFAULT));
    }

    public JwtAuthorizationGenerator(JwtWriterFactory jwtWriterFactory, int jwtExpiryWindowSeconds) {
        this.jwtWriterFactory = checkNotNull(jwtWriterFactory);
        this.jwtExpiryWindowSeconds = jwtExpiryWindowSeconds;
    }

    public Option<String> generate(String httpMethodStr, String url, Map<String, List<String>> parameters, AcHost acHost,
                                   Option<String> userId)
            throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, URISyntaxException {
        final HttpMethod method = HttpMethod.valueOf(httpMethodStr);

        final URI uri = new URI(url);
        final String path = uri.getPath();
        final URI baseUrl = new URI(acHost.getBaseUrl());
        final String productContext = baseUrl.getPath();
        final String pathWithoutProductContext = path.substring(productContext.length());

        LOGGER.trace("Creating Jwt signature for:");
        LOGGER.trace(format("httpMethod: '%s'", httpMethodStr));
        LOGGER.trace(format("URL: '%s'", url));
        LOGGER.trace(format("acHost: '%s'", acHost));
        LOGGER.trace(format("userId: '%s'", userId));
        LOGGER.trace(format("Parameters: %s", parameters));
        LOGGER.trace(format("pathWithoutProductContext: '%s'", pathWithoutProductContext));

        final URI uriWithoutProductContext = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                pathWithoutProductContext, uri.getQuery(), uri.getFragment());

        LOGGER.trace(format("uriWithoutProductContext: '%s'", uriWithoutProductContext));

        return generate(method, uriWithoutProductContext, parameters, acHost, userId);
    }

    public Option<String> generate(HttpMethod httpMethod, URI url, Map<String, List<String>> parameters, AcHost acHost,
                                   Option<String> userId)
            throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException {

        checkArgument(null != parameters, "Parameters Map argument cannot be null");
        checkNotNull(acHost);

        Map<String, String[]> paramsAsArrays = Maps.transformValues(parameters, new Function<List<String>, String[]>() {
            @Override
            public String[] apply(List<String> input) {
                return checkNotNull(input).toArray(new String[input.size()]);
            }
        });
        return Option.some(JWT_AUTH_HEADER_PREFIX + encodeJwt(httpMethod, url, paramsAsArrays, userId.getOrNull(),
                acHost));
    }

    private String encodeJwt(HttpMethod httpMethod, URI targetPath, Map<String, String[]> params, String userKeyValue,
                     AcHost acHost) throws JwtUnknownIssuerException, JwtIssuerLacksSharedSecretException {
        checkArgument(null != httpMethod, "HttpMethod argument cannot be null");
        checkArgument(null != targetPath, "URI argument cannot be null");

        JwtJsonBuilder jsonBuilder = new JsonSmartJwtJsonBuilder()
                .issuedAt(TimeUtil.currentTimeSeconds())
                .expirationTime(TimeUtil.currentTimePlusNSeconds(jwtExpiryWindowSeconds))
                .issuer(AC.PLUGIN_KEY);

        if (null != userKeyValue) {
            jsonBuilder = jsonBuilder.subject(userKeyValue);
        }

        Map<String, String[]> completeParams = params;

        try {
            if (!StringUtils.isEmpty(targetPath.getQuery())) {
                completeParams = new HashMap<String, String[]>(params);
                completeParams.putAll(constructParameterMap(targetPath));
            }

            CanonicalHttpUriRequest canonicalHttpUriRequest = new CanonicalHttpUriRequest(httpMethod.toString(),
                    targetPath.getPath(), "", completeParams);

            LOGGER.debug("Canonical request is: " + CanonicalRequestUtil.toVerboseString(canonicalHttpUriRequest));

            JwtClaimsBuilder.appendHttpRequestClaims(jsonBuilder, canonicalHttpUriRequest);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return issueJwt(jsonBuilder.build(), acHost);
    }


    private String issueJwt(String jsonPayload, AcHost acHost) throws JwtSigningException, JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException {
        return getJwtWriter(acHost).jsonToJwt(jsonPayload);
    }

    private JwtWriter getJwtWriter(AcHost acHost) throws JwtUnknownIssuerException, JwtIssuerLacksSharedSecretException {
        return jwtWriterFactory.macSigningWriter(SigningAlgorithm.HS256, acHost.getSharedSecret());
    }

    private static Map<String, String[]> constructParameterMap(URI uri) throws UnsupportedEncodingException {
        final String query = uri.getQuery();
        if (query == null) {
            return Collections.emptyMap();
        }

        Map<String, String[]> queryParams = new HashMap<String, String[]>();

        CharArrayBuffer buffer = new CharArrayBuffer(query.length());
        buffer.append(query);
        ParserCursor cursor = new ParserCursor(0, buffer.length());

        while (!cursor.atEnd()) {
            NameValuePair nameValuePair = BasicHeaderValueParser.DEFAULT.parseNameValuePair(buffer, cursor, QUERY_DELIMITERS);

            if (!StringUtils.isEmpty(nameValuePair.getName())) {
                String decodedName = urlDecode(nameValuePair.getName());
                String decodedValue = urlDecode(nameValuePair.getValue());
                String[] oldValues = queryParams.get(decodedName);
                String[] newValues = null == oldValues ? new String[1] : Arrays.copyOf(oldValues, oldValues.length + 1);
                newValues[newValues.length - 1] = decodedValue;
                queryParams.put(decodedName, newValues);
            }
        }

        return queryParams;
    }

    private static String urlDecode(final String content) throws UnsupportedEncodingException {
        return null == content ? null : URLDecoder.decode(content, "UTF-8");
    }
}
