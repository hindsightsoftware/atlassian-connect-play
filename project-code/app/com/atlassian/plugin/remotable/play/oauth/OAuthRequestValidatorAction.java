package com.atlassian.plugin.remotable.play.oauth;

import com.atlassian.plugin.remotable.play.Ap3;
import com.atlassian.plugin.remotable.play.PublicKeyStore;
import com.google.common.base.Suppliers;
import models.Ap3Application;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public final class OAuthRequestValidatorAction extends Action.Simple
{
    private final OAuthRequestValidator<Http.Request> validator = new OAuthRequestValidator<>(new PlayRequestHelper(), new PlayPublicKeyStore(), Ap3.baseUrl);

    @Override
    public Result call(Http.Context context) throws Throwable
    {
        try
        {
            final String consumerKey = validator.validate(context.request());
            final Ap3Application host = Ap3Application.findByKey(consumerKey).getOrError(Suppliers.ofInstance("An error occured getting the host application"));

            context.args.put("p3_host", host);

            return delegate.call(context);
        }
        catch (UnknownAp3ApplicationException e)
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
            final Ap3Application host = Ap3Application.findByKey(consumerKey).getOrError(Suppliers.ofInstance("An error occured getting the host application"));
            return host != null ? host.publicKey : null;
        }
    }
}
