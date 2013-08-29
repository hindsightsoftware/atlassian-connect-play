package com.atlassian.connect.play.java.token;


import com.atlassian.connect.play.java.AC;
import com.atlassian.fugue.Option;
import com.atlassian.security.random.DefaultSecureTokenGenerator;
import com.atlassian.security.random.SecureTokenGenerator;
import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.util.concurrent.ConcurrentMap;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;
import static com.atlassian.fugue.Suppliers.alwaysFalse;

public class MemoryTokenStore implements TokenStore
{
    private final ConcurrentMap<TokenKey, Token> tokens = Maps.newConcurrentMap();
    private final SecureTokenGenerator tokenGenerator = DefaultSecureTokenGenerator.getInstance();

    @Override
    public void createIfExpired(final TokenKey key, long requestTime)
    {
        final Token currentToken = tokens.get(key);
        if (hasExpired(currentToken, requestTime))
        {
            //either replace the current token if it exists, or put a new one in place (only if no other thread beat us to it!)
            if (currentToken == null)
            {
                tokens.putIfAbsent(key, new Token(tokenGenerator.generateToken(), requestTime));
            }
            else
            {
                tokens.replace(key, currentToken, new Token(tokenGenerator.generateToken(), requestTime));
            }
        }
    }

    @Override
    public Option<Token> get(final TokenKey key, long requestTime)
    {
        final Token token = tokens.get(key);
        if (hasExpired(token, requestTime))
        {
            return none();
        }
        return some(token);
    }

    @Override
    public boolean validate(final TokenKey key, final String token, final long requestTime)
    {
        final Option<Token> storedToken = get(key, requestTime);
        return storedToken.fold(alwaysFalse(), new Function<Token, Boolean>()
        {
            @Override
            public Boolean apply(final Token storedToken)
            {
                tokens.replace(key, storedToken, storedToken.update(requestTime));
                return storedToken.getToken().equals(token);
            }
        });
    }

    @Override
    public void remove(final TokenKey key)
    {
        tokens.remove(key);
    }

    private boolean hasExpired(final Token token, long requestTime)
    {
        return token == null || (requestTime - token.getLastRequestTime()) > AC.tokenExpiry;
    }
}
