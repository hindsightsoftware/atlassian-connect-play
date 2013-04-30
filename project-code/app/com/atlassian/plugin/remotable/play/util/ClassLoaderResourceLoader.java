package com.atlassian.plugin.remotable.play.util;

import com.atlassian.fugue.Option;

import java.io.InputStream;

import static com.atlassian.fugue.Option.option;
import static com.google.common.base.Preconditions.checkNotNull;

final class ClassLoaderResourceLoader implements ResourceLoader
{
    private final ClassLoader classLoader;

    public ClassLoaderResourceLoader(Class clazz)
    {
        this(checkNotNull(clazz).getClassLoader());
    }

    ClassLoaderResourceLoader(ClassLoader classLoader)
    {
        this.classLoader = checkNotNull(classLoader);
    }

    @Override
    public Option<InputStream> load(String resource)
    {
        return option(classLoader.getResourceAsStream(resource));
    }
}
