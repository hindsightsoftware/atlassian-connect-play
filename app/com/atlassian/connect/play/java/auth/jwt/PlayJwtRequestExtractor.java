package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.connect.play.java.AC;
import com.atlassian.jwt.core.http.AbstractJwtRequestExtractor;
import com.atlassian.jwt.core.http.HttpRequestWrapper;
import com.google.common.base.Supplier;
import play.mvc.Http;

import java.net.URL;

public class PlayJwtRequestExtractor extends AbstractJwtRequestExtractor<Http.Request> {
    private final AddonContextProvider contextProvider;

    public interface AddonContextProvider extends Supplier<String> {
    }

    private static String addonContextPath() {
        try {
            return new URL(AC.baseUrl.get()).getPath();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    PlayJwtRequestExtractor(AddonContextProvider contextProvider) {
        if (contextProvider == null) {
            contextProvider = new AddonContextProvider() {
                @Override
                public String get() {
                    return addonContextPath();
                }
            };

        }
        this.contextProvider = contextProvider;
    }
    @Override
    protected HttpRequestWrapper wrapRequest(Http.Request request) {
        return new PlayRequestWrapper(request, contextProvider.get());
    }
}
