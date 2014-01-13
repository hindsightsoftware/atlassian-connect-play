package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.connect.play.java.AC;
import com.atlassian.connect.play.java.AcHost;
import com.atlassian.connect.play.java.util.Utils;
import com.atlassian.jwt.core.reader.JwtIssuerSharedSecretService;
import com.atlassian.jwt.core.reader.JwtIssuerValidator;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import play.Logger;

public class ACPlayJwtIssuerService implements JwtIssuerSharedSecretService, JwtIssuerValidator {
    private final static Logger.ALogger LOGGER = Utils.LOGGER;

    @Override
    public String getSharedSecret(final String issuer)  {
        return AC.getAcHost(issuer).map(new Function<AcHost, String>()
        {
            @Override
            public String apply(AcHost host)
            {
                String sharedSecret = host.getSharedSecret();
                if (sharedSecret == null) {
                    LOGGER.warn("The issuer " + issuer + " does not have a shared secret in the database. Need to reinstall the addon to the host");
                }
                return sharedSecret;
            }
        }).getOrElse(new Supplier<String>() {
            @Override
            public String get() {
                LOGGER.warn("The issuer " + issuer + " cannot be found in the database. Might be a host that has not registered");
                return null;
            }
        });
    }

    @Override
    public boolean isValid(String issuer) {
        return getSharedSecret(issuer) != null;
    }
}
