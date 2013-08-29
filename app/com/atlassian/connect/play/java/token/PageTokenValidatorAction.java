package com.atlassian.connect.play.java.token;

import com.atlassian.connect.play.java.AC;
import com.atlassian.fugue.Option;
import org.apache.commons.lang3.StringUtils;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import static com.atlassian.connect.play.java.Constants.AC_USER_ID_PARAM;

public final class PageTokenValidatorAction extends Action.Simple
{
    public static final String HEADER_PREFIX = "X-";

    public static final String TOKEN_KEY = "acpt";
    public static final String CONSUMER_KEY = "acck";
    public static final String USER_KEY = "acuid";

    @Override
    public Result call(Http.Context context) throws Throwable
    {
        final F.Tuple<TokenKey, String> tokenDetails = extractTokenDetails(context.request());
        if (tokenDetails == null)
        {
            return unauthorized("Unauthorised: It appears your session has expired. Please reload the page.");
        }
        else if (!AC.validateToken(tokenDetails._1, tokenDetails._2))
        {
            return unauthorized("Unauthorised: It appears your session has expired. Please reload the page.");
        }

        AC.setAcHost(tokenDetails._1.getConsumerKey());
        final Option<String> user = tokenDetails._1.getUser();
        if(user.isDefined())
        {
            context.args.put(AC_USER_ID_PARAM, user.get());
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
