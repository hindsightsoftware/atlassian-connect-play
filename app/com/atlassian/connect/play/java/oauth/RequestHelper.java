package com.atlassian.connect.play.java.oauth;

import com.atlassian.connect.play.java.BaseUrl;
import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Map;

interface RequestHelper<R>
{
    String getHttpMethod(R request);

    String getUrl(R request, BaseUrl baseUrl);

    Multimap<String, String> getParameters(R request);

    Option<String> getHeader(R request, String name);
}
