package com.atlassian.connect.play.java.oauth;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.PublicKeyStore;
import com.google.common.base.Suppliers;
import models.AcHostModel;
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
            final String consumerKey = validator.validate(context.request());
            final AcHost host = AcHostModel.findByKey(consumerKey).getOrError(Suppliers.ofInstance("An error occured getting the host application"));

            context.args.put("ac_host", host);

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
            final AcHostModel host = AcHostModel.findByKey(consumerKey).getOrError(Suppliers.ofInstance("An error occured getting the host application"));
            return host != null ? host.publicKey : null;
        }
    }
}
