package com.atlassian.connect.play.java.token;

import com.atlassian.fugue.Option;

/**
 * Session like store for secure tokens per tenant and user id.  This token store can be used to authenticate subsequent
 * requests after an iframe has been requested with the appropriate OAuth headers.
 * <p/>
 * The general flow is as follows:
 * <ul>
 *     <li>OAuth signed iframe request is received.  This triggers a call to {@link #createIfExpired(TokenKey, long)}</li>
 *     <li>Subsequent requests without OAuth headers can be verified by calling {@link #validate(TokenKey, String, long)} (see {@link CheckValidPageToken}</li>
 *     <li>Pages served should include the current page token obtained via {@link #get(TokenKey, long)} and include it either via request parameters or request headers</li>
 *     <li>When the Atlassian host notifies the remote app that a logout has occurred, {@link #remove(TokenKey)} should be called for this user</li>
 * </ul>
 * <p/>
 * Tokens have a lifetime (configurable via ac.token.expiry.secs).  Every time a token is validate, this lifetime is extended.  Once no requests are retrieved
 * for longer than the token lifetime validate should return false and get should return none.
 * <p/>
 * Implementations may choose to store tokens in memory (which could cause issues for horizontal scaling, as well as performance issues on large instances) or configure
 * custom token stores via the ac.token.store property (to use something like Redis for example).
 */
public interface TokenStore
{
    void createIfExpired(final TokenKey key, long requestTime);

    Option<Token> get(final TokenKey key, long requestTime);

    boolean validate(final TokenKey key, String token, long requestTime);

    void remove(final TokenKey key);
}
