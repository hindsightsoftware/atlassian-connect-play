package com.atlassian.connect.play.java.auth;

public final class InvalidAuthenticationRequestException extends AuthenticationRequestException
{
    public InvalidAuthenticationRequestException(String msg)
    {
        super(msg);
    }
}