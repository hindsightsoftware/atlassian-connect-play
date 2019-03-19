package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.auth.InvalidAuthenticationRequestException;
import com.atlassian.jwt.Jwt;
import com.atlassian.jwt.core.http.auth.JwtAuthenticator;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Result;

import static play.libs.F.Either;
import static play.mvc.Http.Context;
import static play.mvc.Http.Request;
import static play.mvc.Http.Response;

public final class JwtRequestAuthenticatorAction extends Action.Simple
{
    private static final JwtAuthenticator<Request, Response, JwtAuthenticationResult> authenticator = JwtAuthConfig.getJwtAuthenticator();

    @Override
    public Promise<Result> call(Context context) throws Throwable
    {
        return new AuthenticationHelper().authenticate(context, delegate);
    }

    // exists to make it easier to test
    static class AuthenticationHelper {
        public Promise<Result> authenticate(Context context, Action delegate) throws Throwable
        {
            try
            {
                Either<Status, Jwt> authResult = authenticator.authenticate(context.request(), context.response()).getResult();
                if (authResult.left.isDefined()) {
                    return Promise.pure((Result)authResult.left.get());
                }

                Jwt jwt = authResult.right.get();
                AC.setAcHost(jwt.getIssuer());

                JsonNode payload = Json.parse(jwt.getJsonPayload());
                if (payload.hasNonNull("context") && payload.get("context").hasNonNull("user")) {
                    JsonNode userObject = payload.get("context").get("user");
                    if (userObject.hasNonNull("userKey")) {
                        AC.setUser(userObject.get("userKey").asText());
                    }
                    if (userObject.hasNonNull("accountId")) {
                        AC.setUserAccountId(userObject.get("accountId").asText());
                    }
                }

                AC.refreshToken(false);

                return delegate.call(context);
            }
            catch (InvalidAuthenticationRequestException e)
            {
                return Promise.pure((Result)badRequest("Bad request: " + e.getMessage()));
            }
        }

    }
}
