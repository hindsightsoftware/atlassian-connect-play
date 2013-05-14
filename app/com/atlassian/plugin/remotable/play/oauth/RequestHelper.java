package com.atlassian.plugin.remotable.play.oauth;

import com.atlassian.plugin.remotable.play.BaseUrl;

import java.util.Collection;
import java.util.Map;

interface RequestHelper<R>
{
    String getHttpMethod(R request);

    String getUrl(R request, BaseUrl baseUrl);

    String getParameter(R request, String name);

    Collection<? extends Map.Entry> getParameters(R request);
}
