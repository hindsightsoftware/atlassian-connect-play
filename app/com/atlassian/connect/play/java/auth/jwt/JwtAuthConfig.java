package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.jwt.core.http.auth.JwtAuthenticator;
import com.atlassian.jwt.core.reader.NimbusJwtReaderFactory;
import com.atlassian.jwt.reader.JwtReaderFactory;
import play.mvc.Http;

import static play.mvc.Http.Request;
import static play.mvc.Http.Response;

// TODO: add spring DI or similar
public class JwtAuthConfig {
    private static ACPlayJwtIssuerService acPlayJwtIssuerService = new ACPlayJwtIssuerService();

    private static final PlayJwtAuthenticator jwtAuthenticator =
            new PlayJwtAuthenticator(new PlayJwtRequestExtractor(), new PlayAuthenticationResultHandler(),
                    createReaderFactory());

    private static JwtReaderFactory createReaderFactory() {
        return new NimbusJwtReaderFactory(acPlayJwtIssuerService, acPlayJwtIssuerService);
    }

    public static JwtAuthenticator<Request, Response, JwtAuthenticationResult> getJwtAuthenticator() {
        return jwtAuthenticator;
    }
}
