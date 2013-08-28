package com.atlassian.connect.play.java.oauth;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.PublicKeyStore;
import com.google.common.base.Function;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public final class OAuthRequestValidatorAction extends Action.Simple
{
    private final OAuthRequestValidator<Http.Request> validator = new OAuthRequestValidator<>(new PlayRequestHelper(), new PlayPublicKeyStore(), AC.baseUrl);

    @Override
    public Result call(Http.Context context) throws Throwable
    {
        try
        {
            AC.setAcHost(validator.validate(context.request()));

            return delegate.call(context);
        }
        catch (UnknownAcHostException e)
        {
            return badRequest("Unknown host for consumer key: " + e.getConsumerKey());
        }
        catch (InvalidOAuthRequestException e)
        {
            return badRequest("Bad request: " + e.getMessage());
        }
        catch (UnauthorisedOAuthRequestException e)
        {
            return unauthorized("Unauthorised: " + e.getMessage());
        }
    }

    private static final class PlayPublicKeyStore implements PublicKeyStore
    {
        public String getPublicKey(String consumerKey)
        {
            return AC.getAcHost(consumerKey).map(new Function<AcHost, String>()
            {
                @Override
                public String apply(AcHost host)
                {
                    return host.getPublicKey();
                }
            }).getOrNull();
        }
    }
}
