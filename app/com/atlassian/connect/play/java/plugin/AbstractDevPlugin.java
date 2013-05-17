package com.atlassian.connect.play.java.plugin;

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
