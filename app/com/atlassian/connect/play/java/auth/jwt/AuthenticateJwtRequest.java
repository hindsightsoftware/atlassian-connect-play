package com.atlassian.connect.play.java.auth.jwt;

import play.mvc.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method as requiring Jwt authentication. Requests originating from an Atlassian host product via Atlassian Connect
 * will be signed using Jwt. This annotation will ensure validation is performed for that request. On successful validation
 * the user will be set in the AC context.
 * On authentication failure the request will be denied with a suitable Http status code.
 */
@With(JwtRequestAuthenticatorAction.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticateJwtRequest
{
}

