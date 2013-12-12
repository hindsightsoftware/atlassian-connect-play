package com.atlassian.connect.play.java.auth.oauth;

public final class InvalidOAuthRequestException extends RuntimeException
{
    public InvalidOAuthRequestException(String msg)
    {
        super(msg);
    }
}