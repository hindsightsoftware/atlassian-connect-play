package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.jwt.core.http.auth.JwtAuthenticator;
import com.atlassian.jwt.core.reader.NimbusJwtReaderFactory;
import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.jwt.reader.JwtReaderFactory;
import com.atlassian.jwt.writer.JwtWriterFactory;
import play.mvc.Http;

import static play.mvc.Http.Request;
import static play.mvc.Http.Response;

// TODO: add spring DI or similar
public class JwtAuthConfig {
    private static ACPlayJwtIssuerService acPlayJwtIssuerService = new ACPlayJwtIssuerService();

    private static final PlayJwtAuthenticator jwtAuthenticator =
            new PlayJwtAuthenticator(new PlayJwtRequestExtractor(), new PlayAuthenticationResultHandler(),
                    createReaderFactory());

    private static JwtAuthorizationGenerator jwtAuthorizationGenerator =
            new JwtAuthorizationGenerator(acPlayJwtIssuerService, createWriterFactory());

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
