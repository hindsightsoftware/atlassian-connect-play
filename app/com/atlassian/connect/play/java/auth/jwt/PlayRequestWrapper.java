package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.jwt.CanonicalHttpRequest;
import com.atlassian.jwt.core.http.HttpRequestWrapper;
import play.mvc.Http;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;

public class PlayRequestWrapper implements HttpRequestWrapper {
    private final Http.Request request;

    public PlayRequestWrapper(Http.Request request) {

        this.request = request;
    }

    @Nullable
    @Override
    public String getParameter(String parameterName) {
        return request.getQueryString(parameterName);
    }

    @Override
    public Iterable<String> getHeaderValues(String headerName) {
        return Arrays.asList(request.headers().get(headerName));
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
                return request.path();
            }

            @Nonnull
            @Override
            public Map<String, String[]> getParameterMap() {
                return request.queryString();
            }
        };
    }
}
