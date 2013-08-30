package com.atlassian.connect.play.java.token;

import com.atlassian.connect.play.java.AC;
import com.atlassian.fugue.Option;
import org.apache.commons.lang3.StringUtils;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import static com.atlassian.connect.play.java.Constants.AC_USER_ID_PARAM;
import static com.atlassian.fugue.Option.option;

public final class PageTokenValidatorAction extends Action.Simple
{
    public static final String HEADER_PREFIX = "X-";

    public static final String TOKEN_KEY = "acpt";

    @Override
    public Result call(final Http.Context context) throws Throwable
    {
        final Option<String> token = extractTokenDetails(context.request());
        if (token.isEmpty())
        {
            return unauthorized("Unauthorised: It appears your session has expired. Please reload the page.");
        }
        final Option<Token> decryptedToken = AC.validateToken(token.get());
        if (decryptedToken.isEmpty())
        {
            return unauthorized("Unauthorised: It appears your session has expired. Please reload the page.");
        }

        AC.setAcHost(decryptedToken.get().getAcHost());
        final Option<String> user = decryptedToken.get().getUser();
        if (user.isDefined())
        {
            context.args.put(AC_USER_ID_PARAM, user.get());
        }

        //valid request so lets refresh the token with a new timestamp and add it to the response headers
        //so clients can update their tokens on ajax responses!
        AC.refreshToken();
        context.response().setHeader(HEADER_PREFIX + TOKEN_KEY, AC.getToken().get());
        return delegate.call(context);
    }

    private Option<String> extractTokenDetails(final Http.Request request)
    {
        String token = request.getHeader(HEADER_PREFIX + TOKEN_KEY);
        if (StringUtils.isBlank(token))
        {
            token = request.getQueryString(TOKEN_KEY);
        }
        return option(token);
    }
}
