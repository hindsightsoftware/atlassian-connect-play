package com.atlassian.plugin.remotable.play.plugin;

import play.Application;

import static com.atlassian.plugin.remotable.play.util.Utils.LOGGER;

public final class PluginKeyPlugin extends AbstractPlugin
{
    public PluginKeyPlugin(Application application)
    {
        super(application);
    }

    @Override
    public void onStart()
    {
        final String pluginKey = application.configuration().getString("ap3.key");
        if (pluginKey == null)
        {
            LOGGER.error("Configuration ap3.key must be configured with your plugin key. Please add this to your conf/application.conf");
            throw new IllegalStateException("Plugin key must be defined, see error message above for more information");
        }
        else
        {
            LOGGER.debug("Plugin key is set to " + pluginKey);
        }
    }
}
