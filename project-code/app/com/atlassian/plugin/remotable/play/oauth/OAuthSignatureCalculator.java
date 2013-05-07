package com.atlassian.plugin.remotable.play.oauth;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.remotable.play.Ap3;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.signature.RSA_SHA1;
import play.libs.WS;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OAuthSignatureCalculator implements WS.SignatureCalculator
{
    public static final String USER_ID_QUERY_PARAMETER = "user_id";

    private static final Supplier<OAuthConsumer> LOCAL_CONSUMER = Suppliers.memoize(new Supplier<OAuthConsumer>()
    {
        @Override
        public OAuthConsumer get()
        {
            return loadLocalConsumer();
        }
    });

    private final Option<String> user;

    public OAuthSignatureCalculator(Option<String> user)
    {
        this.user = checkNotNull(user);
    }

    @Override
    public void sign(WS.WSRequest request)
    {

        request.setHeader("Authorization", getAuthorizationHeaderValue(request));
    }

    public String getAuthorizationHeaderValue(WS.WSRequest request) throws IllegalArgumentException
    {
        try
        {
            final OAuthConsumer localConsumer = LOCAL_CONSUMER.get();
            final Map<String, String> params = addOAuthParameters(request, localConsumer);
            if (user.isDefined())
            {
                params.put(USER_ID_QUERY_PARAMETER, user.get());
            }

            final OAuthMessage oauthMessage = new OAuthMessage(request.getMethod(), request.getUrl(), params.entrySet());
            oauthMessage.sign(new OAuthAccessor(localConsumer));
            return oauthMessage.getAuthorizationHeader(null);
        }
        catch (OAuthException e)
        {
            // shouldn't really happen...
            throw new IllegalArgumentException("Failed to sign the request", e);
        }
        catch (IOException | URISyntaxException e)
        {
            // this shouldn't happen as the message is not being read from any IO streams, but the OAuth library throws
            // these around like they're candy, but far less sweet and tasty.
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> addOAuthParameters(WS.WSRequest request, final OAuthConsumer local)
    {
        final HashMap<String, String> params = Maps.newHashMap();
        addOAuthParameter(request, params, OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);
        addOAuthParameter(request, params, OAuth.OAUTH_VERSION, "1.0");
        addOAuthParameter(request, params, OAuth.OAUTH_CONSUMER_KEY, local.consumerKey);
        addOAuthParameter(request, params, OAuth.OAUTH_NONCE, getNonce());
        addOAuthParameter(request, params, OAuth.OAUTH_TIMESTAMP, getTimestamp());

        return params;
    }

    private void addOAuthParameter(WS.WSRequest request, Map<String, String> params, String key, String value)
    {
        request.addParameter(key, value);
        params.put(key, value);
    }

    private String getNonce()
    {
        return System.nanoTime() + "";
    }

    private static String getTimestamp()
    {
        return System.currentTimeMillis() / 1000 + "";
    }

    private static OAuthConsumer loadLocalConsumer()
    {
        final OAuthServiceProvider serviceProvider = new OAuthServiceProvider(null, null, null);
        final OAuthConsumer localConsumer = new OAuthConsumer(null, Ap3.PLUGIN_KEY, null, serviceProvider);
        localConsumer.setProperty(RSA_SHA1.PRIVATE_KEY, Ap3.privateKey.get());
        localConsumer.setProperty(RSA_SHA1.PUBLIC_KEY, Ap3.publicKey.get());
        return localConsumer;
    }
}
