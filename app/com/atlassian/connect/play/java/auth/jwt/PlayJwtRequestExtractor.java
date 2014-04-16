package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.connect.play.java.AC;
import com.atlassian.jwt.core.http.AbstractJwtRequestExtractor;
import com.atlassian.jwt.core.http.HttpRequestWrapper;
import com.google.common.base.Supplier;
import play.mvc.Http;

import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;

public class PlayJwtRequestExtractor extends AbstractJwtRequestExtractor<Http.Request> {
    private final AddonContextProvider contextProvider;

    public interface AddonContextProvider extends Supplier<String> {
    }

    PlayJwtRequestExtractor(AddonContextProvider contextProvider) {
        this.contextProvider = checkNotNull(contextProvider);
    }
    @Override
    protected HttpRequestWrapper wrapRequest(Http.Request request) {
        return new PlayRequestWrapper(request, contextProvider.get());
    }
}
