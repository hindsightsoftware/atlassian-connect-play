package com.atlassian.plugin.remotable.play;

import com.atlassian.plugin.remotable.play.util.Environment;

public final class Ap3
{
    // the base URL
    public static BaseUrl baseUrl;

    public static final PublicKey publicKey = new PublicKey()
    {
        @Override
        public String get()
        {
            return Environment.getEnv("OAUTH_LOCAL_PUBLIC_KEY");
        }
    };
}
