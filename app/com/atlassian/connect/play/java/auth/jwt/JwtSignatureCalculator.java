package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.connect.play.java.AcHost;
import com.atlassian.fugue.Option;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import play.libs.ws.WSRequest;
import play.libs.ws.WSSignatureCalculator;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public final class JwtSignatureCalculator implements WSSignatureCalculator
{
    private final JwtAuthorizationGenerator jwtAuthorizationGenerator;
    private final AcHost acHost;
    private final Option<String> userId;

    public JwtSignatureCalculator(JwtAuthorizationGenerator jwtAuthorizationGenerator, AcHost acHost, Option<String> userId) {
        this.jwtAuthorizationGenerator = checkNotNull(jwtAuthorizationGenerator);
        this.acHost = checkNotNull(acHost);
        this.userId = userId;
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
            Option<String> jwt = jwtAuthorizationGenerator.generate(request.getMethod(), request.getUrl(),
                    getQueryParams(request), acHost, userId);
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

    private Map<String, List<String>> getQueryParams(WSRequest request)
    {
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUrl());
        return decoder.getParameters();
    }

}
