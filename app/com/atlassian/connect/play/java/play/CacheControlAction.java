package com.atlassian.connect.play.java.play;

import com.atlassian.connect.play.java.AC;
import com.google.common.base.Function;

import play.Play;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import javax.annotation.Nullable;

import static com.atlassian.fugue.Option.option;
import static play.mvc.Http.HeaderNames.CACHE_CONTROL;

public final class CacheControlAction extends Action<WithCacheControl>
{
    private static final String DEFAULT_CACHE_CONTROL = Play.application().configuration().getString("ac.cache-control", AC.isDev() ? "no-cache" : null);

    public Promise<Result> call(Http.Context ctx) throws Throwable
    {
        if (!ctx.args.containsKey(CACHE_CONTROL))
        {
            ctx.args.put(CACHE_CONTROL, getCacheControl());
        }

        final Promise<Result> result = delegate.call(ctx);

        final String cacheControl = (String) ctx.args.get(CACHE_CONTROL);
        final Http.Response response = ctx.response();
        if (cacheControl != null && !responseCacheControlIsSet(response))
        {
            response.setHeader(CACHE_CONTROL, cacheControl);
        }
        return result;
    }

    public String getCacheControl()
    {
        return option(configuration).map(
                new Function<WithCacheControl, String>()
                {
                    @Nullable
                    @Override
                    public String apply(WithCacheControl annotation)
                    {
                        return annotation.value();
                    }
                })
                .getOrElse(DEFAULT_CACHE_CONTROL);
    }

    private static boolean responseCacheControlIsSet(Http.Response response)
    {
        return response.getHeaders().containsKey(CACHE_CONTROL);
    }
}
