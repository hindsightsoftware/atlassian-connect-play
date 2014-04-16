package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.connect.play.java.AC;
import com.atlassian.jwt.core.http.auth.JwtAuthenticator;
import com.atlassian.jwt.core.reader.NimbusJwtReaderFactory;
import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.jwt.reader.JwtReaderFactory;
import com.atlassian.jwt.writer.JwtWriterFactory;

import java.net.MalformedURLException;
import java.net.URL;

import static play.mvc.Http.Request;
import static play.mvc.Http.Response;

// TODO: add spring DI or similar
public class JwtAuthConfig {
    private static ACPlayJwtIssuerService acPlayJwtIssuerService = new ACPlayJwtIssuerService();

    private static String addonContextPath() {
        try {
            return new URL(AC.baseUrl.get()).getPath();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static final PlayJwtRequestExtractor.AddonContextProvider contextProvider =
            new PlayJwtRequestExtractor.AddonContextProvider() {
                @Override
                public String get() {
                    return addonContextPath();
                }
            };

    private static final PlayJwtAuthenticator jwtAuthenticator =
            new PlayJwtAuthenticator(new PlayJwtRequestExtractor(contextProvider), new PlayAuthenticationResultHandler(),
                    createReaderFactory());

    private static JwtAuthorizationGenerator jwtAuthorizationGenerator =
            new JwtAuthorizationGenerator(createWriterFactory());

    private static JwtReaderFactory createReaderFactory() {
        return new NimbusJwtReaderFactory(acPlayJwtIssuerService, acPlayJwtIssuerService);
    }

    private static JwtWriterFactory createWriterFactory() {
        return new NimbusJwtWriterFactory();
    }

    public static JwtAuthenticator<Request, Response, JwtAuthenticationResult> getJwtAuthenticator() {
        return jwtAuthenticator;
    }

    public static JwtAuthorizationGenerator getJwtAuthorizationGenerator() {
        return jwtAuthorizationGenerator;
    }
}
