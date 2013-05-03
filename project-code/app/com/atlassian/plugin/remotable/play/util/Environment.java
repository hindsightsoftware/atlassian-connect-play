package com.atlassian.plugin.remotable.play.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public final class Environment
{
    public static final String OAUTH_LOCAL_PUBLIC_KEY = "OAUTH_LOCAL_PUBLIC_KEY";
    public static final String OAUTH_LOCAL_PRIVATE_KEY = "OAUTH_LOCAL_PRIVATE_KEY";

    private static final Map<String, String> env = loadEnv(ImmutableList.of(new ResourcePropertiesLoader("/env.properties", new ClassResourceLoader(Environment.class)), new EnvironmentPropertiesLoader()));

    public static String getEnv(String name)
    {
        String val = getOptionalEnv(name, null);
        if (val == null)
        {
            throw new IllegalArgumentException("Missing environment variable: " + name);
        }
        else
        {
            return val;
        }
    }

    public static String getOptionalEnv(String name, String def)
    {
        final String val = env.get(name);
        if (val == null)
        {
            return def;
        }
        else
        {
            return val.replaceAll("\\\\n", "\n");
        }
    }

    private static ImmutableMap<String, String> loadEnv(Iterable<PropertiesLoader> propertiesLoaders)
    {
        final Map<String, String> envBuilder = newHashMap();
        for (PropertiesLoader properties : propertiesLoaders)
        {
            envBuilder.putAll(properties.load());
        }
        return ImmutableMap.copyOf(envBuilder);
    }
}
