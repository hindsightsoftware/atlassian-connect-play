package com.atlassian.connect.play.java.plugin;

import com.atlassian.connect.play.java.AC;
import play.Application;

import static com.atlassian.connect.play.java.util.Utils.LOGGER;
import static java.lang.String.format;

public final class PluginKeyPlugin extends AbstractPlugin
{
    public PluginKeyPlugin(Application application)
    {
        super(application);
    }

    @Override
    public void onStart()
    {
        if (AC.PLUGIN_KEY == null)
        {
            LOGGER.error("Property 'ac.key' must be configured with your add-on key. Please add this to your 'conf/application.conf'");
            throw new IllegalStateException("Add-on key must be defined, see error message above for more information");
        }
        else
        {
            LOGGER.debug(format("Add-on key is set to '%s'", AC.PLUGIN_KEY));
        }
    }
}
