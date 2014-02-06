package com.atlassian.connect.play.java.util;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AllJsDataOptionsTest {

    @Test
    public void returnsEmptyStringWhenNoPropertiesSet() {
        assertThat(new AllJsDataOptions().toString(), is(""));
    }

    @Test
    public void marginSerialisesCorrectly() {
        assertThat(new AllJsDataOptions().margin(true).toString(), is("margin:true"));
    }

    @Test
    public void baseSerialisesCorrectly() {
        assertThat(new AllJsDataOptions().base(true).toString(), is("base:true"));
    }

    @Test
    public void resizeSerialisesCorrectly() {
        assertThat(new AllJsDataOptions().resize(true).toString(), is("resize:true"));
    }

    @Test
    public void sizeToParentSerialisesCorrectly() {
        assertThat(new AllJsDataOptions().sizeToParent(true).toString(), is("sizeToParent:true"));
    }

    @Test
    public void adhocPropertiesWork() {
        assertThat(new AllJsDataOptions().add("foo", "bar").toString(), is("foo:bar"));
    }

    @Test
    public void nullValueComesOutAsBlank() {
        assertThat(new AllJsDataOptions().add("foo", null).toString(), is("foo:"));
    }

    @Test
    public void twoPropertiesHasNoSeparatorBetween() {
        assertThat(new AllJsDataOptions().margin(true).base(true).toString(), is("base:true;margin:true"));
    }
}
