package com.atlassian.plugin.remotable.play.oauth;

import static com.google.common.base.Preconditions.checkNotNull;

public final class UnknownAp3ApplicationException extends RuntimeException
{
    private final String consumerKey;

    public UnknownAp3ApplicationException(String consumerKey)
    {
        this.consumerKey = checkNotNull(consumerKey);
    }

    public String getConsumerKey()
    {
        return consumerKey;
    }
}
