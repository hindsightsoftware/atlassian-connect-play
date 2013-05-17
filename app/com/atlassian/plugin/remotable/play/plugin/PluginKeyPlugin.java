package com.atlassian.plugin.remotable.play.plugin;

import com.atlassian.plugin.remotable.play.Ap3;
import play.Application;

import static com.atlassian.plugin.remotable.play.util.Utils.LOGGER;
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
        if (Ap3.PLUGIN_KEY == null)
        {
            LOGGER.error("Property 'ap3.key' must be configured with your add-on key. Please add this to your 'conf/application.conf'");
            throw new IllegalStateException("Add-on key must be defined, see error message above for more information");
        }
        else
        {
            LOGGER.debug(format("Add-on key is set to '%s'", Ap3.PLUGIN_KEY));
        }
    }
}
