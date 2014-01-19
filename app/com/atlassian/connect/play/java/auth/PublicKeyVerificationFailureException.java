package com.atlassian.connect.play.java.auth;

public final class PublicKeyVerificationFailureException extends AuthenticationRequestException
{
    public PublicKeyVerificationFailureException(String msg)
    {
        super(msg);
    }
}