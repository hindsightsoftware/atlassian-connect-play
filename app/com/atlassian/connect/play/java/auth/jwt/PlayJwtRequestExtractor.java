package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.jwt.core.http.AbstractJwtRequestExtractor;
import com.atlassian.jwt.core.http.HttpRequestWrapper;
import play.mvc.Http;

public class PlayJwtRequestExtractor extends AbstractJwtRequestExtractor<Http.Request> {
    @Override
    protected HttpRequestWrapper wrapRequest(Http.Request request) {
        return new PlayRequestWrapper(request);
    }
}
