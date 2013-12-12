package com.atlassian.connect.play.java.auth;

import com.atlassian.connect.play.java.auth.AuthenticationRequestException;

public final class UnauthorisedRequestException extends AuthenticationRequestException
{
    public UnauthorisedRequestException(String msg, Throwable throwable)
    {
        super(msg, throwable);
    }
}
