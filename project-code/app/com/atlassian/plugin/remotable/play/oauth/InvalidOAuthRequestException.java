package com.atlassian.plugin.remotable.play.oauth;

public final class InvalidOAuthRequestException extends RuntimeException
{
    public InvalidOAuthRequestException(String msg)
    {
        super(msg);
    }
}