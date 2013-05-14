package com.atlassian.plugin.remotable.play.plugin;

public interface Ap3KeyPair<T>
{
    T getPublicKey();

    T getPrivateKey();
}
