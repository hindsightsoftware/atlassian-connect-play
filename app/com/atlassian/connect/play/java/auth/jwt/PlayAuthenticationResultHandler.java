package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.jwt.Jwt;
import com.atlassian.jwt.core.http.auth.AuthenticationResultHandler;
import com.atlassian.jwt.core.http.auth.JwtAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;

import static play.mvc.Http.Response;
import static play.mvc.Http.Status.*;
import static play.mvc.Results.status;

public class PlayAuthenticationResultHandler implements AuthenticationResultHandler<Response, JwtAuthenticationResult> {
    private static final Logger log = LoggerFactory.getLogger(PlayAuthenticationResultHandler.class);

    @Override
    public JwtAuthenticationResult createAndSendInternalError(Exception e, Response response, String externallyVisibleMessage)
    {
        // the internal error could give away runtime details that could be useful in an attack, so don't display it externally
        return createError(e, INTERNAL_SERVER_ERROR, externallyVisibleMessage);
    }

    @Override
    public JwtAuthenticationResult createAndSendBadRequestError(Exception e, Response response, String externallyVisibleMessage)
    {
        // the message will probably be seen by add-on vendors during add-on development
        return createError(e, BAD_REQUEST, externallyVisibleMessage);
    }

    @Override
    public JwtAuthenticationResult createAndSendUnauthorisedFailure(Exception e, Response response, String externallyVisibleMessage)
    {
        // the jwt has good syntax but was rejected, and was not rejected due to the user or issuer specifically
        return createFailure(e, UNAUTHORIZED, externallyVisibleMessage);
    }

    @Override
    public JwtAuthenticationResult createAndSendForbiddenError(Exception e, Response response)
    {
        // this is the default error response, so the message is quite general
        return createError(e, FORBIDDEN, "Access to this resource is forbidden without successful authentication. Please supply valid credentials.");
    }

    @Override
    public JwtAuthenticationResult success(String message, Principal principal, Jwt authenticatedJwt)
    {
        return new JwtAuthenticationResult(authenticatedJwt);
    }

    private static JwtAuthenticationResult createError(Exception e, int httpResponseCode, String externallyVisibleMessage)
    {
        log.debug("Error during JWT authentication: ", e);
        return createErrorResult(httpResponseCode, externallyVisibleMessage);
    }

    private static JwtAuthenticationResult createFailure(Exception e, int httpResponseCode, String externallyVisibleMessage)
    {
        log.debug("Failure during JWT authentication: ", e);
        return createErrorResult(httpResponseCode, externallyVisibleMessage);
    }

    private static JwtAuthenticationResult createErrorResult(int httpResponseCode, String externallyVisibleMessage)
    {
        return new JwtAuthenticationResult(status(httpResponseCode, externallyVisibleMessage));
    }

}
