package com.atlassian.plugin.remotable.play;

import com.atlassian.plugin.remotable.play.util.Environment;
import com.google.common.io.Files;
import play.Logger;
import play.Play;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.atlassian.plugin.remotable.play.util.Environment.OAUTH_LOCAL_PRIVATE_KEY;
import static com.atlassian.plugin.remotable.play.util.Environment.OAUTH_LOCAL_PUBLIC_KEY;
import static java.lang.String.format;

public final class Ap3
{
    // the base URL
    public static BaseUrl baseUrl;

    public static final PublicKey publicKey = new PublicKey()
    {
        @Override
        public String get()
        {
            return getKey(OAUTH_LOCAL_PUBLIC_KEY, "public-key.pem");
        }
    };

    public static final PublicKey privateKey = new PublicKey()
    {
        @Override
        public String get()
        {
            return getKey(OAUTH_LOCAL_PRIVATE_KEY, "private-key.pem");
        }
    };

    private static String getKey(String envKey, String fileName)
    {
        final String key = Environment.getOptionalEnv(envKey, null);
        if (key != null)
        {
            return key;
        }
        else if (Play.isDev())
        {
            try
            {
                return getFileContent(fileName);
            }
            catch (IOException e)
            {
                Logger.warn(format("Could not read '%s' file.", fileName), e);
            }
        }
        throw new IllegalStateException(format("Could NOT find %s for OAuth!", envKey));
    }

    private static String getFileContent(String pathname) throws IOException
    {
        final StringBuilder sb = new StringBuilder();
        Files.copy(new File(pathname), Charset.forName("UTF-8"), sb);
        return sb.toString();
    }
}
