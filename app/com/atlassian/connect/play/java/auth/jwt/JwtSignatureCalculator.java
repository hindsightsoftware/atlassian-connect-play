package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.connect.play.java.AC;
import com.atlassian.fugue.Option;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.ning.http.client.FluentStringsMap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;

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
            Option<String> jwt = jwtAuthorizationGenerator.generate(request.getMethod(), request.getUrl(), getQueryParams(request), AC.getAcHostOrThrow(), AC.getUser());
            return jwt.getOrNull();
        }
        catch (JwtIssuerLacksSharedSecretException | JwtUnknownIssuerException e)
        {
            // shouldn't really happen...
            throw new IllegalArgumentException("Failed to sign the request", e);
        }
        catch (URISyntaxException e)
        {
            // this will happen if the baseUrl is invalid
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
