package com.atlassian.connect.play.java.token;

public final class Token
{
    private final String token;
    private final long lastRequestTime;

    public Token(final String token, final long lastRequestTime)
    {
        this.token = token;
        this.lastRequestTime = lastRequestTime;
    }

    public String getToken()
    {
        return token;
    }

    public long getLastRequestTime()
    {
        return lastRequestTime;
    }

    public Token update(long lastRequestTime)
    {
        return new Token(this.token, lastRequestTime);
    }
}
