package com.atlassian.plugin.remotable.play;

public interface PublicKeyStore
{
    String getPublicKey(String consumerKey);
}