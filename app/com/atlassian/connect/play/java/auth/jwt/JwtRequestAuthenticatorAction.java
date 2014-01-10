package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.PublicKeyStore;
import com.atlassian.connect.play.java.auth.InvalidAuthenticationRequestException;
import com.atlassian.connect.play.java.auth.UnauthorisedRequestException;
import com.atlassian.connect.play.java.auth.UnknownAcHostException;
import com.atlassian.jwt.Jwt;
import com.atlassian.jwt.core.http.auth.JwtAuthenticator;
import com.google.common.base.Function;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;

import static play.libs.F.Either;
import static play.mvc.Http.Context;
import static play.mvc.Http.Request;
import static play.mvc.Http.Response;

public final class JwtRequestAuthenticatorAction extends Action.Simple
{
    private static final JwtAuthenticator<Request, Response, JwtAuthenticationResult> authenticator = JwtAuthConfig.getJwtAuthenticator();

    @Override
    public Promise<SimpleResult> call(Context context) throws Throwable
    {
        return new AuthenticationHelper().authenticate(context, delegate);
    }

    // exists to make it easier to test
    static class AuthenticationHelper {
        public Promise<SimpleResult> authenticate(Context context, Action delegate) throws Throwable
        {
            try
            {
                Either<Status, Jwt> authResult = authenticator.authenticate(context.request(), context.response()).getResult();
                if (authResult.left.isDefined()) {
                    return Promise.pure((SimpleResult)authResult.left.get());
                }

                Jwt jwt = authResult.right.get();
                AC.setAcHost(jwt.getIssuer());
                AC.setUser(jwt.getSubject());
                AC.refreshToken(false);

                return delegate.call(context);

                // TODO: make sure these are no longer used and remove them
            }
            catch (UnknownAcHostException e)
            {
                return Promise.pure((SimpleResult)badRequest("Unknown host for consumer key: " + e.getConsumerKey()));
            }
            catch (InvalidAuthenticationRequestException e)
            {
                return Promise.pure((SimpleResult)badRequest("Bad request: " + e.getMessage()));
            }
            catch (UnauthorisedRequestException e)
            {
                return Promise.pure((SimpleResult)unauthorized("Unauthorised: " + e.getMessage()));
            }
        }

    }
}
