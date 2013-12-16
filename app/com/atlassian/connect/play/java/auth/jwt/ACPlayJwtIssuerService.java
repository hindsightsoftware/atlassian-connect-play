package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.PublicKeyStore;
import com.atlassian.jwt.core.reader.JwtIssuerSharedSecretService;
import com.atlassian.jwt.core.reader.JwtIssuerValidator;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.google.common.base.Function;

public class ACPlayJwtIssuerService implements JwtIssuerSharedSecretService, JwtIssuerValidator {

    @Override
    public String getSharedSecret(String issuer) throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException {
        return AC.getAcHost(issuer).map(new Function<AcHost, String>()
        {
            @Override
            public String apply(AcHost host)
            {
                return host.getPublicKey(); // TODO: may need to change this name (assuming we store the shared secret in the same column as the public key for oauth)
            }
        }).getOrNull();
    }

    @Override
    public boolean isValid(String issuer) {
        try {
            return getSharedSecret(issuer) != null;
        } catch (JwtIssuerLacksSharedSecretException e) {
            // TODO: log or something
            return false;
        } catch (JwtUnknownIssuerException e) {
            // TODO: log or something
            return false;
        }
    }
}
