package com.atlassian.connect.play.java.play;

import play.GlobalSettings;
import play.mvc.Action;
import play.mvc.Http;

import java.lang.reflect.Method;

public class AcGlobalSettings extends GlobalSettings
{
    @Override
    public Action onRequest(final Http.Request request, final Method actionMethod)
    {
        return new CacheControlAction();
    }
}
