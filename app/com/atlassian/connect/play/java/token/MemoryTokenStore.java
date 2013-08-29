package com.atlassian.connect.play.java.token;


import com.atlassian.connect.play.java.AC;
import com.atlassian.fugue.Option;
import com.atlassian.security.random.DefaultSecureTokenGenerator;
import com.atlassian.security.random.SecureTokenGenerator;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;

public class MemoryTokenStore implements TokenStore
{
    final Map<TokenKey, Token> tokens = Maps.newHashMap();
    final ReadWriteLock lock = new ReentrantReadWriteLock();
    final SecureTokenGenerator tokenGenerator = DefaultSecureTokenGenerator.getInstance();

    @Override
    public String createIfExpired(final TokenKey key, long requestTime)
    {
        lock.readLock().lock();
        try
        {
            Token token = tokens.get(key);
            if (!isValid(token, requestTime))
            {
                lock.readLock().unlock();
                lock.writeLock().lock();
                try
                {
                    token = tokens.get(key);
                    if (!isValid(token, requestTime))
                    {
                        token = new Token(tokenGenerator.generateToken(), requestTime);
                        tokens.put(key, token);
                    }
                    lock.readLock().lock();
                }
                finally
                {
                    lock.writeLock().unlock();
                }
            }
            return token.getToken();
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    @Override
    public Option<String> get(final TokenKey key, long requestTime)
    {
        lock.readLock().lock();
        try
        {
            final Token token = tokens.get(key);
            if (isValid(token, requestTime))
            {
                return some(token.getToken());
            }
            return none();
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean isValid(final TokenKey key, final String token, final long requestTime)
    {
        final Option<String> storedToken = get(key, requestTime);
        return storedToken.isDefined() && storedToken.get().equals(token);
    }

    @Override
    public void remove(final TokenKey key)
    {
        lock.writeLock().lock();
        try
        {
            tokens.remove(key);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    private boolean isValid(final Token token, long requestTime)
    {
        return token != null && (requestTime - token.getLastRequestTime()) <= AC.tokenExpiry;
    }
}
