package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.PublicKeyStore;
import com.atlassian.jwt.Jwt;
import com.atlassian.jwt.core.http.JwtRequestExtractor;
import com.atlassian.jwt.core.http.auth.AbstractJwtAuthenticator;
import com.atlassian.jwt.core.http.auth.AuthenticationResultHandler;
import com.atlassian.jwt.core.http.auth.SimplePrincipal;
import com.atlassian.jwt.exception.*;
import com.atlassian.jwt.reader.JwtClaimVerifier;
import com.atlassian.jwt.reader.JwtReaderFactory;
import com.google.common.base.Function;
import play.mvc.Http;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Map;

import static play.mvc.Http.Request;
import static play.mvc.Http.Response;

public class PlayJwtAuthenticator extends AbstractJwtAuthenticator<Request, Response, JwtAuthenticationResult> {
    private final JwtReaderFactory jwtReaderFactory;

    public PlayJwtAuthenticator(JwtRequestExtractor<Request> jwtExtractor,
                                AuthenticationResultHandler<Response, JwtAuthenticationResult> authenticationResultHandler,
                                JwtReaderFactory jwtReaderFactory) {
        super(jwtExtractor, authenticationResultHandler);
        this.jwtReaderFactory = jwtReaderFactory;
    }

    // TODO: where was the oauth validators setting the principal?
    @Override
    protected Principal authenticate(Request request, Jwt jwt) throws JwtUserRejectedException {
        Principal userPrincipal = new SimplePrincipal(jwt.getSubject());
        // TODO: don't think we need to validate user further. If they are ok'ed by the host we trust them right??
        return userPrincipal;
    }

    @Override
    protected Jwt verifyJwt(String jwt, Map<String, ? extends JwtClaimVerifier> claimVerifiers) throws JwtParseException,
            JwtVerificationException, JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, IOException, NoSuchAlgorithmException {
        Jwt verifiedJwt = jwtReaderFactory.getReader(jwt).read(jwt, claimVerifiers);
        // TODO: do I need further verification? The ACPlayIssuerService will have checked issuer in db etc
        return verifiedJwt;
    }


}
