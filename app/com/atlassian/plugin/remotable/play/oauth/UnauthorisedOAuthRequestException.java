package com.atlassian.plugin.remotable.play.oauth;

public final class UnauthorisedOAuthRequestException extends RuntimeException
{
    public UnauthorisedOAuthRequestException(String msg, Throwable throwable)
    {
        super(msg, throwable);
    }
}
