package com.atlassian.connect.play.java;

import com.google.common.annotations.VisibleForTesting;

public interface AcHost
{
    public abstract Long getId();

    public abstract String getKey();

    public abstract String getName();

    public abstract String getDescription();

    public abstract String getBaseUrl();

    public abstract String getPublicKey();

    public abstract String getSharedSecret();

    public abstract void setKey(String key);

    public abstract void setName(String name);

    public abstract void setBaseUrl(String baseUrl);

    public abstract void setPublicKey(String publicKey);

    public abstract void setSharedSecret(String secret);
}
