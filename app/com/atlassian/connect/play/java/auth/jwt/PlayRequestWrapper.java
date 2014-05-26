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
        // Header retrieval must be case insensitive
        // (http://www.w3.org/Protocols/HTTP/1.1/rfc2616bis/draft-lafon-rfc2616bis-02.html#message.headers)
        // Note the following code was copied and adapted from RequestHeader.getHeader()
        String[] headerValues = null;
        final Map<String, String[]> headers = request.headers();
        for(String h: headers.keySet()) {
            if(headerName.toLowerCase().equals(h.toLowerCase())) {
                headerValues = headers.get(h);
                break;
            }
        }
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
