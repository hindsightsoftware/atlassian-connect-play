package com.atlassian.plugin.remotable.play.oauth;

import com.atlassian.plugin.remotable.play.BaseUrl;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import play.mvc.Http;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static java.lang.String.format;

public final class PlayRequestHelper implements RequestHelper<Http.Request>
{
    public String getHttpMethod(Http.Request request)
    {
        return request.method();
    }

    public String getUrl(Http.Request request, BaseUrl baseUrl)
    {
        return baseUrl.get() + request.path();
    }

    public String getParameter(Http.Request request, String name)
    {
        final String[] values = request.queryString().get(name);

        if (values == null)
        {
            throw new InvalidOAuthRequestException(format("Could not find parameter %s for request!", name));
        }

        if (values.length != 1)
        {
            throw new InvalidOAuthRequestException(format("Found unexpected values %s for parameter %s", Arrays.toString(values), name));
        }

        return values[0];
    }

    public Collection<? extends Map.Entry> getParameters(Http.Request request)
    {
        final Multimap<String, String> map = ArrayListMultimap.create();
        for (Map.Entry<String, String[]> entry : request.queryString().entrySet())
        {
            for (String v : entry.getValue())
            {
                final String k = entry.getKey();
                map.put(k, v);
            }
        }
        return map.entries();
    }
}
