package com.atlassian.connect.play.java.auth;

import com.atlassian.connect.play.java.BaseUrl;
import com.atlassian.connect.play.java.PublicKeyStore;
import com.atlassian.fugue.Option;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import net.oauth.OAuth;
import net.oauth.OAuthMessage;

import java.util.Collection;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public abstract class AbstractRequestValidator<R> implements RequestValidator<R> {
    protected final RequestHelper<R> requestHelper;
    private final PublicKeyStore publicKeyStore;
    protected final BaseUrl baseUrl;

    /**
     * @param requestHelper the helper to extract information from the given type of request.
     * @param publicKeyStore the store to the public key, used to check the authentication signature.
     * @param baseUrl the base URL of the remote app, this should return the same URL as the one found in the
     * {@code atlassian-remote-app.xml} descriptor.
     */
    public AbstractRequestValidator(RequestHelper<R> requestHelper, PublicKeyStore publicKeyStore, BaseUrl baseUrl)
    {
        this.requestHelper = checkNotNull(requestHelper);
        this.publicKeyStore = checkNotNull(publicKeyStore);
        this.baseUrl = checkNotNull(baseUrl);
    }

    protected Multimap<String, String> getParameters(R request)
    {
        final ImmutableMultimap.Builder<String, String> parameters =
                ImmutableMultimap.<String, String>builder().putAll(requestHelper.getParameters(request));

        final Option<String> authorization = requestHelper.getHeader(request, "Authorization");
        if (authorization.isDefined())
        {
            for (OAuth.Parameter param : OAuthMessage.decodeAuthorization(authorization.get()))
            {
                parameters.put(param.getKey(), param.getValue());
            }
        }
        return parameters.build();
    }

    protected String getConsumerKey(Multimap<String, String> parameters)
    {
        final Collection<String> consumerKeys = parameters.get("oauth_consumer_key");
        checkState(consumerKeys.size() == 1, "There should be only one value for the consumer key");
        String consumerKey = Iterables.getFirst(consumerKeys, null);
        LOGGER.debug("Found consumer key '" + consumerKey + "'.");
        return consumerKey;
    }

    protected String fetchPublicKey(String consumerKey) {
        final String publicKey = publicKeyStore.getPublicKey(consumerKey);
        if (publicKey == null)
        {
            throw new UnknownAcHostException(consumerKey);
        }
        return publicKey;
    }

}
