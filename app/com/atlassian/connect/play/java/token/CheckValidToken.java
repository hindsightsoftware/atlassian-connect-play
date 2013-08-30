package com.atlassian.connect.play.java.token;

import play.mvc.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@With (PageTokenValidatorAction.class)
@Target ({ ElementType.TYPE, ElementType.METHOD })
@Retention (RetentionPolicy.RUNTIME)
public @interface CheckValidToken
{
    /**
     * By default when an Action is annotated with this annotations, insecure polling is turned off.  That is, there's
     * no way for a client to refresh the token after the page has been served initially via the iframe with OAuth
     * headers.
     * <p/>
     * Some applications however may continuously want to poll the server. If this is necessary then this flag can be
     * set to true which will include a fresh token in the response headers sent to the client.  <em>CAUTION</em>: This
     * is inherently insecure since a malicious party could request fresh tokens indefinitely once they have managed to
     * obtain such a token to start with.  Only methods annotated with this flag set to true will accept tokens that
     * were refreshed.
     */
    boolean allowInsecurePolling() default false;
}

