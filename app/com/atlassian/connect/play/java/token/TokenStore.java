package com.atlassian.connect.play.java.token;

import com.atlassian.fugue.Option;

/**
 *
 */
public interface TokenStore
{
    String createIfExpired(final TokenKey key, long requestTime);

    Option<String> get(final TokenKey key, long requestTime);

    boolean isValid(final TokenKey key, String token, long requestTime);

    void remove(final TokenKey key);
}
