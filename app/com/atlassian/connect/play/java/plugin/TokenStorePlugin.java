package com.atlassian.connect.play.java.plugin;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.token.MemoryTokenStore;
import com.atlassian.connect.play.java.token.TokenStore;
import org.apache.commons.lang3.StringUtils;
import play.Application;

import java.util.concurrent.TimeUnit;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;

/**
 * Takes care of initialising the pluggable token store
 */
public class TokenStorePlugin extends AbstractPlugin
{
    public TokenStorePlugin(final Application application)
    {
        super(application);
    }

    @Override
    public void onStart()
    {
        final String tokenStoreImplClass = application.configuration().getString("ac.token.store");
        final String tokenExpiry = application.configuration().getString("ac.token.expiry.secs");
        if (StringUtils.isNotBlank(tokenExpiry) && StringUtils.isNumeric(tokenExpiry))
        {
            AC.tokenExpiry = TimeUnit.MILLISECONDS.convert(Long.parseLong(tokenExpiry), TimeUnit.SECONDS);
        }
        else
        {
            AC.tokenExpiry = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);
        }

        if (StringUtils.isNotBlank(tokenStoreImplClass))
        {
            try
            {
                AC.tokenStore = (TokenStore) Class.forName(tokenStoreImplClass).newInstance();
            }
            catch (ClassCastException | ClassNotFoundException | InstantiationException | IllegalAccessException e)
            {
                LOGGER.error("Token store implementation '" + tokenStoreImplClass + "' "
                        + "could not be found or instantiated! Switching to in memory token store.", e);
                AC.tokenStore = new MemoryTokenStore();
            }
        }
        else
        {
            AC.tokenStore = new MemoryTokenStore();
        }
        super.onStart();
    }
}
