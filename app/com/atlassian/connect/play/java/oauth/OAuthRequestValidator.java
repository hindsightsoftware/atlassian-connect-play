package com.atlassian.connect.play.java.oauth;

import com.atlassian.connect.play.java.BaseUrl;
import com.atlassian.connect.play.java.PublicKeyStore;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.SimpleOAuthValidator;
import net.oauth.signature.RSA_SHA1;
import play.Logger;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;

final class OAuthRequestValidator<R>
{
    private final RequestHelper<R> requestHelper;
    private final PublicKeyStore publicKeyStore;
    private final BaseUrl baseUrl;

    /**
     * @param requestHelper the helper to extract information from the given type of request.
     * @param publicKeyStore the store to the public key, used to check the OAuth signature.
     * @param baseUrl the base URL of the remote app, this should return the same URL as the one found in the
     * {@code atlassian-remote-app.xml} descriptor.
     */
    public OAuthRequestValidator(RequestHelper<R> requestHelper, PublicKeyStore publicKeyStore, BaseUrl baseUrl)
    {
        this.requestHelper = checkNotNull(requestHelper);
        this.publicKeyStore = checkNotNull(publicKeyStore);
        this.baseUrl = checkNotNull(baseUrl);
    }

    /**
     * Validate the given request as an OAuth request. This method will return normally if the request is valid, it will
     * throw an exception otherwise.
     *
     * @param request the request to validate
     * @return the OAuth consumer key set in the request.
     * @throws InvalidOAuthRequestException if the request is invalid.
     */
    public String validate(R request)
    {
        final String consumerKey = getConsumerKey(request);

        final OAuthMessage message = new OAuthMessage(
                requestHelper.getHttpMethod(request),
                requestHelper.getUrl(request, baseUrl),
                requestHelper.getParameters(request));
        try
        {
            final OAuthConsumer host = new OAuthConsumer(null, consumerKey, null, null);
            final String publicKey = publicKeyStore.getPublicKey(consumerKey);
            if (publicKey == null)
            {
                throw new UnknownAcHostException(consumerKey);
            }

            host.setProperty(RSA_SHA1.PUBLIC_KEY, publicKey);
            message.validateMessage(new OAuthAccessor(host), new SimpleOAuthValidator());

            return consumerKey;
        }
        catch (OAuthProblemException e)
        {
            Logger.warn("The request is not a valid OAuth request", e);
//            return consumerKey;
            throw new UnauthorisedOAuthRequestException(String.format("Validation failed: \nproblem: %s\nparameters: %s\n", e.getProblem(), e.getParameters()), e);
        }
        catch (OAuthException | IOException | URISyntaxException e)
        {
            Logger.error("An error happened validating the OAuth request.", e);
            throw new RuntimeException(e);
        }
    }

    private String getConsumerKey(R request)
    {
        final String consumerKey = requestHelper.getParameter(request, "oauth_consumer_key");
        Logger.debug("Found consumer key '" + consumerKey + "'.");
        return consumerKey;
    }
}
