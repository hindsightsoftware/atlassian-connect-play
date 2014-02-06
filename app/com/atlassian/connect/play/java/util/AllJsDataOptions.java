package com.atlassian.connect.play.java.util;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Encapsulates the values that can be passed in the data-options attribute of the script for all.js
 *
 * Example
 *
 * <pre>
 *     new AllJsDataOptions()
 *         .margin(true)
 *         .resize(true);
 * </pre>
 */
public class AllJsDataOptions {
    /**
     * add a margin to the body tag
     */
    private Map<String, Object> options = Maps.newHashMap();

    public AllJsDataOptions margin(boolean margin) {
        return add("margin", margin);
    }

    /**
     * add a <base /> tag to the head
     */
    public AllJsDataOptions base(boolean base) {
        return add("base", base);
    }

    /**
     * size the iframe to the size of the page (if on a general page)
     */
    public AllJsDataOptions sizeToParent(boolean sizeToParent) {
        return add("sizeToParent", sizeToParent);
    }

    /**
     * enable / disable the autoresizer
     */
    public AllJsDataOptions resize(boolean resize) {
        return add("resize", resize);
    }

    /**
     * Adds another property. Note this is just here in case a new data option is added to all.js in the future.
     * We recommend you use the explicit property setters unless the option you want is not included yet
     */
    public AllJsDataOptions add(String key, Object value) {
        options.put(key, value);
        return this;
    }


    @Override
    public String toString() {
        Iterable<String> keyValues = Iterables.transform(options.entrySet(), new Function<Map.Entry<String, Object>, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Map.Entry<String, Object> entry) {
                return entry.getKey() + ":" + (entry.getValue() == null ? "" : entry.getValue());
            }
        });
        return Joiner.on(';').join(keyValues);
    }
}

