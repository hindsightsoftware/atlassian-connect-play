package com.atlassian.connect.play.java.util;

import com.atlassian.fugue.Option;

import java.io.InputStream;

import static com.atlassian.fugue.Option.option;
import static com.google.common.base.Preconditions.checkNotNull;

final class ClassResourceLoader implements ResourceLoader
{
    private final Class clazz;

    public ClassResourceLoader(Class clazz)
    {
        this.clazz = checkNotNull(clazz);
    }

    @Override
    public Option<InputStream> load(String resource)
    {
        return option(clazz.getResourceAsStream(resource));
    }
}
