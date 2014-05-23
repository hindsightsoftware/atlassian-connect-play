package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.jwt.CanonicalHttpRequest;
import com.atlassian.jwt.core.http.HttpRequestWrapper;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import play.mvc.Http;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;

public class PlayRequestWrapper implements HttpRequestWrapper {
    private final Http.Request request;
    private final String addonContext;

    public PlayRequestWrapper(Http.Request request, String addonContext) {

        this.request = request;
        this.addonContext = addonContext;
    }

    @Nullable
    @Override
    public String getParameter(String parameterName) {
        return request.getQueryString(parameterName);
    }

    @Override
    public Iterable<String> getHeaderValues(String headerName) {
        final String[] headerValues = request.headers().get(headerName);
        return headerValues != null ? Arrays.asList(headerValues) : ImmutableList.<String>of();
    }

    @Override
    public CanonicalHttpRequest getCanonicalHttpRequest() {
        return new CanonicalHttpRequest() {
            @Nonnull
            @Override
            public String getMethod() {
                return request.method();
            }

            @Nullable
            @Override
            public String getRelativePath() {
                return StringUtils.removeStart(request.path(), addonContext);
            }

            @Nonnull
            @Override
            public Map<String, String[]> getParameterMap() {
                return request.queryString();
            }
        };
    }
}
