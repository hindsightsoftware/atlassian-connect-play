package com.atlassian.connect.play.java;

import com.google.common.annotations.VisibleForTesting;

public abstract class AcHost
{
    @VisibleForTesting
    public static final String CONSUMER_INFO_URL = "/plugins/servlet/oauth/consumer-info";

    public abstract Long getId();

    public abstract String getKey();

    public abstract String getName();

    public abstract String getDescription();

    public abstract String getBaseUrl();

    public abstract String getPublicKey();

    public abstract String getSharedSecret();

    public String getConsumerInfoUrl()
    {
        return getBaseUrl() + CONSUMER_INFO_URL;
    }

    public abstract void setKey(String key);

    public abstract void setName(String name);

    public abstract void setBaseUrl(String baseUrl);

    public abstract void setPublicKey(String publicKey);

    public abstract void setSharedSecret(String secret);
}
