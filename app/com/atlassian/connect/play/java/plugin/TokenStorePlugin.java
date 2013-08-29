package com.atlassian.connect.play.java.plugin;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.token.MemoryTokenStore;
import com.atlassian.connect.play.java.token.TokenStore;
import com.atlassian.fugue.Option;
import org.apache.commons.lang3.StringUtils;
import play.Application;

import java.util.concurrent.TimeUnit;

import static com.atlassian.connect.play.java.Constants.AC_TOKEN_EXPIRY;
import static com.atlassian.connect.play.java.Constants.AC_TOKEN_STORE;
import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;

/**
 * Takes care of initialising the pluggable token store
 */
public class TokenStorePlugin extends AbstractPlugin
{
    private static final long DEFAULT_TOKEN_EXPIRY = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);

    public TokenStorePlugin(final Application application)
    {
        super(application);
    }

    @Override
    public void onStart()
    {
        final String tokenStoreImplClass = application.configuration().getString(AC_TOKEN_STORE);
        AC.tokenExpiry = getConfiguredTokenExpiry().getOrElse(DEFAULT_TOKEN_EXPIRY);

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

    private Option<Long> getConfiguredTokenExpiry()
    {
        final String tokenExpiry = application.configuration().getString(AC_TOKEN_EXPIRY);
        if (StringUtils.isNotBlank(tokenExpiry) && StringUtils.isNumeric(tokenExpiry))
        {
            return some(TimeUnit.MILLISECONDS.convert(Long.parseLong(tokenExpiry), TimeUnit.SECONDS));
        }
        return none();
    }
}
