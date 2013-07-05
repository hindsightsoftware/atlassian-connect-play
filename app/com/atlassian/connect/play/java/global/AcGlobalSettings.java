package com.atlassian.connect.play.java.global;

import play.GlobalSettings;
import play.Play;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.lang.reflect.Method;

public class AcGlobalSettings extends GlobalSettings
{
    @Override
    public Action onRequest(final Http.Request request, final Method actionMethod)
    {
        return new Action.Simple()
        {
            public Result call(Http.Context ctx) throws Throwable
            {
                Result call = delegate.call(ctx);
                if (Play.isDev())
                {
                    ctx.response().setHeader(Http.HeaderNames.CACHE_CONTROL, "no-cache");
                }
                return call;
            }
        };
    }
}
