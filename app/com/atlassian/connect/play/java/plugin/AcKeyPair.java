package com.atlassian.connect.play.java.plugin;

public interface AcKeyPair<T>
{
    T getPublicKey();

    T getPrivateKey();
}
