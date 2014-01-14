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

import static com.google.common.base.Preconditions.checkNotNull;
import static play.mvc.Http.Request;
import static play.mvc.Http.Response;

/**
 * A JwtAuthenticator for the Play framework
 */
public class PlayJwtAuthenticator extends AbstractJwtAuthenticator<Request, Response, JwtAuthenticationResult> {
    private final JwtReaderFactory jwtReaderFactory;

    public PlayJwtAuthenticator(JwtRequestExtractor<Request> jwtExtractor,
                                AuthenticationResultHandler<Response, JwtAuthenticationResult> authenticationResultHandler,
                                JwtReaderFactory jwtReaderFactory) {
        super(jwtExtractor, authenticationResultHandler);
        this.jwtReaderFactory = checkNotNull(jwtReaderFactory);
    }

    @Override
    protected Principal authenticate(Request request, Jwt jwt) throws JwtUserRejectedException {
        // we don't do any further validation of the user here. If the host vouches for them then ok by us
        return jwt.getSubject() == null ? null : new SimplePrincipal(jwt.getSubject());
    }

    @Override
    protected Jwt verifyJwt(String jwt, Map<String, ? extends JwtClaimVerifier> claimVerifiers) throws JwtParseException,
            JwtVerificationException, JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, IOException, NoSuchAlgorithmException {
        return jwtReaderFactory.getReader(jwt).read(jwt, claimVerifiers);
    }


}
