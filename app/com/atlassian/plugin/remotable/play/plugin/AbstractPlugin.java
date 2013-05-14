package com.atlassian.plugin.remotable.play.plugin;

import play.Application;
import play.Plugin;

import static com.google.common.base.Preconditions.checkNotNull;

abstract class AbstractPlugin extends Plugin
{
    final Application application;

    AbstractPlugin(Application application)
    {
        this.application = checkNotNull(application);
    }
}
