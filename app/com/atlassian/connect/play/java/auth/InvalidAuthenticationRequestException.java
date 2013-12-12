package com.atlassian.connect.play.java.auth;

import com.atlassian.connect.play.java.auth.AuthenticationRequestException;

public final class InvalidAuthenticationRequestException extends AuthenticationRequestException
{
    public InvalidAuthenticationRequestException(String msg)
    {
        super(msg);
    }
}