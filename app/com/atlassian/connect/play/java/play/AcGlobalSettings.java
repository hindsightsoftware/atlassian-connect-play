package com.atlassian.connect.play.java.play;

import com.google.common.collect.ObjectArrays;
import play.GlobalSettings;
import play.api.mvc.EssentialFilter;
import play.filters.gzip.GzipFilter;
import play.mvc.Action;
import play.mvc.Http;

import java.lang.reflect.Method;

public class AcGlobalSettings extends GlobalSettings
{
    @Override
    @SuppressWarnings("unchecked")
    public <T extends EssentialFilter> Class<T>[] filters()
    {
        return (Class[]) ObjectArrays.concat(GzipFilter.class, super.filters());
    }

    @Override
    public Action<WithCacheControl> onRequest(final Http.Request request, final Method actionMethod)
    {
        return new CacheControlAction();
    }
}
