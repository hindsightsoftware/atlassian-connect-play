package com.atlassian.connect.play.java.plugin;

import com.atlassian.connect.play.java.AC;
import com.atlassian.fugue.Option;
import org.apache.commons.lang3.StringUtils;
import play.Application;

import java.util.concurrent.TimeUnit;

import static com.atlassian.connect.play.java.Constants.AC_TOKEN_EXPIRY;
import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;

/**
 * Takes care of initialising the pluggable token store
 */
public class TokenStorePlugin extends AbstractPlugin
{
    private static final long DEFAULT_TOKEN_EXPIRY = TimeUnit.MILLISECONDS.convert(15, TimeUnit.MINUTES);

    public TokenStorePlugin(final Application application)
    {
        super(application);
    }

    @Override
    public void onStart()
    {
        AC.tokenExpiry = getConfiguredTokenExpiry().getOrElse(DEFAULT_TOKEN_EXPIRY);

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
