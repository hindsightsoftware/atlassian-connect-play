package com.atlassian.connect.play.java.auth;

/**
 * Indicates that the public key passed in an install request does not match the public key of the host the addon is to be installed in
 */
public final class MismatchPublicKeyException extends AuthenticationRequestException
{
    public MismatchPublicKeyException(String providedPublicKey, String fetchedPublicKey) {
        super("The public key provided in the install request does not match that on the host. " +
                "The provided public key is\n" + providedPublicKey +
                "\nThe host public key is\n" + fetchedPublicKey);
    }
}