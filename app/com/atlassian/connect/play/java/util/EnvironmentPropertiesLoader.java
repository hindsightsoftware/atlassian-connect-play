package com.atlassian.connect.play.java.util;

import java.util.Map;

/**
 * A properties loader to load environment properties.
 */
final class EnvironmentPropertiesLoader implements PropertiesLoader
{
    @Override
    public Map<String, String> load()
    {
        return System.getenv();
    }
}
