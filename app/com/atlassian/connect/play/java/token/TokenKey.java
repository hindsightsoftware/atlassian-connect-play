package com.atlassian.connect.play.java.token;

import com.atlassian.fugue.Option;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TokenKey
{
    private final String consumerKey;
    private final Option<String> user;

    public TokenKey(final String consumerKey, final Option<String> user)
    {
        this.consumerKey = checkNotNull(consumerKey);
        this.user = checkNotNull(user);
    }

    public String getConsumerKey()
    {
        return consumerKey;
    }

    public Option<String> getUser()
    {
        return user;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final TokenKey tokenKey = (TokenKey) o;

        if (!consumerKey.equals(tokenKey.consumerKey)) { return false; }
        if (!user.equals(tokenKey.user)) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = consumerKey.hashCode();
        result = 31 * result + user.hashCode();
        return result;
    }
}
