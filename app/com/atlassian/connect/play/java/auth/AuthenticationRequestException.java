package com.atlassian.connect.play.java.auth;

public abstract class AuthenticationRequestException extends RuntimeException
{
    public AuthenticationRequestException(String msg)
    {
        super(msg);
    }

    public AuthenticationRequestException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}