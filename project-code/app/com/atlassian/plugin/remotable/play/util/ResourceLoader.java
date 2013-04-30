package com.atlassian.plugin.remotable.play.util;

import com.atlassian.fugue.Option;

import java.io.InputStream;

interface ResourceLoader
{
    Option<InputStream> load(String resource);
}
