package com.atlassian.connect.play.java.oauth;

import com.atlassian.connect.play.java.BaseUrl;

import java.util.Collection;
import java.util.Map;

interface RequestHelper<R>
{
    String getHttpMethod(R request);

    String getUrl(R request, BaseUrl baseUrl);

    String getParameter(R request, String name);

    Collection<? extends Map.Entry> getParameters(R request);
}
