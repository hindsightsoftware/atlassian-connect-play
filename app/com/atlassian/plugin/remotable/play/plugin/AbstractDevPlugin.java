package com.atlassian.plugin.remotable.play.plugin;

import play.Application;


abstract class AbstractDevPlugin extends AbstractPlugin
{
    AbstractDevPlugin(Application application)
    {
        super(application);
    }

    @Override
    public final boolean enabled()
    {
        return application.isDev();
    }
}
