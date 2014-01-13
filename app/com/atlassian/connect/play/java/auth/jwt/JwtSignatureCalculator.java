package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.http.HttpMethod;
import com.atlassian.fugue.Option;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.ning.http.client.FluentStringsMap;
import net.oauth.*;
import net.oauth.signature.RSA_SHA1;
import play.libs.WS;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static play.libs.WS.SignatureCalculator;
import static play.libs.WS.WSRequest;

public final class JwtSignatureCalculator implements SignatureCalculator
{
    private final JwtAuthorizationGenerator jwtAuthorizationGenerator;

    public JwtSignatureCalculator(JwtAuthorizationGenerator jwtAuthorizationGenerator) {
        this.jwtAuthorizationGenerator = checkNotNull(jwtAuthorizationGenerator);
    }

    @Override
    public void sign(WSRequest request)
    {
        final String authorizationHeaderValue = getAuthorizationHeaderValue(request);
        LOGGER.debug(format("Generated Jwt authorisation header: '%s'", authorizationHeaderValue));
        request.setHeader("Authorization", authorizationHeaderValue);
    }

    public String getAuthorizationHeaderValue(WSRequest request) throws IllegalArgumentException
    {
        try
        {
            final FluentStringsMap params = getQueryParams(request);

            final HttpMethod method = HttpMethod.valueOf(request.getMethod());
            final String url = request.getUrl();

            final URI uri = new URI(url);
            final String pathWithoutProductContext = uri.getPath().substring(url.indexOf('/', 1));
            final URI uriWithoutProductContext = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                    pathWithoutProductContext, uri.getQuery(), uri.getFragment());
            final AcHost acHost = AC.getAcHost();
            final Option<String> userId = AC.getUser();

            LOGGER.debug("Creating Jwt signature for:");
            LOGGER.debug(format("Method: '%s'", method));
            LOGGER.debug(format("URL: '%s'", url));
            LOGGER.debug(format("uriWithoutProductContext: '%s'", uriWithoutProductContext));
            LOGGER.debug(format("acHost: '%s'", acHost));
            LOGGER.debug(format("userId: '%s'", userId));
            LOGGER.debug(format("Parameters: %s", params));

            Option<String> jwt = jwtAuthorizationGenerator.generate(method, uriWithoutProductContext, params, acHost, userId);
            return jwt.getOrNull();
        }
        catch (JwtIssuerLacksSharedSecretException | JwtUnknownIssuerException e)
        {
            // shouldn't really happen...
            throw new IllegalArgumentException("Failed to sign the request", e);
        }
        catch (URISyntaxException e)
        {
            // this shouldn't happen as the message is not being read from any IO streams, but the OAuth library throws
            // these around like they're candy, but far less sweet and tasty.
            throw new RuntimeException(e);
        }
    }

    // TODO: Figure out if we need this. Think it is hacking around play not exposing query params
    // Copied from OAuthRequestValidator

    private FluentStringsMap getQueryParams(WSRequest request)
    {
        final Object underlyingRequest = getRequestObject(getRequestField(request), request);
        return getQueryParams(getGetQueryParamsMethod(underlyingRequest), underlyingRequest);
    }

    private FluentStringsMap getQueryParams(Method m, Object request)
    {
        try
        {
            return (FluentStringsMap) m.invoke(request);
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Method getGetQueryParamsMethod(Object o)
    {
        try
        {
            final Method m = o.getClass().getMethod("getQueryParams");
            m.setAccessible(true);
            return m;
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Field getRequestField(WSRequest request)
    {
        try
        {
            final Field f = request.getClass().getSuperclass().getDeclaredField("request");
            f.setAccessible(true);
            return f;
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Object getRequestObject(Field f, WSRequest request)
    {
        try
        {
            return f.get(request);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }
}
