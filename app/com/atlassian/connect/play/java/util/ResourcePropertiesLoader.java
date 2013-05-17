package com.atlassian.connect.play.java.util;

import com.google.common.base.Function;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Closeables;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

final class ResourcePropertiesLoader implements PropertiesLoader
{
    private final String resource;
    private final ResourceLoader loader;

    public ResourcePropertiesLoader(String resource)
    {
        this(resource, new ClassLoaderResourceLoader(ResourcePropertiesLoader.class));
    }

    public ResourcePropertiesLoader(String resource, ResourceLoader loader)
    {
        this.resource = checkNotNull(resource);
        this.loader = checkNotNull(loader);
    }

    @Override
    public Map<String, String> load()
    {
        return loader.load(resource).fold(
                Suppliers.ofInstance(Collections.<String, String>emptyMap()),
                new Function<InputStream, Map<String, String>>()
                {
                    @Override
                    public Map<String, String> apply(InputStream input)
                    {
                        return loadProperties(input);
                    }
                }
        );
    }

    private Map<String, String> loadProperties(InputStream input)
    {
        try
        {
            final Properties props = new Properties();
            props.load(input);
            return toMap(props);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            Closeables.closeQuietly(input);
        }
    }

    private Map<String, String> toMap(Properties props)
    {
        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (Map.Entry<Object, Object> p : props.entrySet())
        {
            builder.put(p.getKey().toString(), p.getValue().toString());
        }
        return builder.build();
    }
}
