package com.atlassian.connect.play.java.token;

import com.atlassian.connect.play.java.AC;
import com.atlassian.fugue.Option;
import org.apache.commons.lang3.StringUtils;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public final class PageTokenValidatorAction extends Action.Simple
{
    public static final String HEADER_PREFIX = "X-ac-";

    public static final String TOKEN_KEY = "page-view-token";
    public static final String CONSUMER_KEY = "consumer_key";
    public static final String USER_KEY = "user_id";

    @Override
    public Result call(Http.Context context) throws Throwable
    {
        final F.Tuple<TokenKey, String> tokenDetails = extractTokenDetails(context.request());
        if (tokenDetails == null)
        {
            return unauthorized("Unauthorised: no valid page token could be found");
        }
        else if (!AC.tokenStore.isValid(tokenDetails._1, tokenDetails._2, System.currentTimeMillis()))
        {
            return unauthorized("Unauthorised: no valid page token could be found");
        }

        AC.setAcHost(tokenDetails._1.getConsumerKey());
        final Option<String> user = tokenDetails._1.getUser();
        if(user.isDefined())
        {
            context.args.put("user_id", user.get());
        }
        return delegate.call(context);
    }

    private F.Tuple<TokenKey, String> extractTokenDetails(final Http.Request request)
    {
        String token = request.getHeader(HEADER_PREFIX + TOKEN_KEY);
        if (StringUtils.isNotBlank(token))
        {
            final String consumerKey = request.getHeader(HEADER_PREFIX + CONSUMER_KEY);
            final Option<String> user = Option.option(request.getHeader(HEADER_PREFIX + USER_KEY));
            return F.Tuple(new TokenKey(consumerKey, user), token);
        }
        else
        {
            //try extracting it via request parameters otherwise
            token = request.getQueryString(TOKEN_KEY);
            if (StringUtils.isNotBlank(token))
            {
                final String consumerKey = request.getQueryString(CONSUMER_KEY);
                final Option<String> user = Option.option(request.getQueryString(USER_KEY));
                return F.Tuple(new TokenKey(consumerKey, user), token);
            }
        }
        return null;
    }
}
