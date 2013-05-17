package com.atlassian.connect.play.java.util;

import com.atlassian.fugue.Option;

import java.io.InputStream;

interface ResourceLoader
{
    Option<InputStream> load(String resource);
}
