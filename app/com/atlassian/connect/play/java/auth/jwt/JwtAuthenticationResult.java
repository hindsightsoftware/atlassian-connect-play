package com.atlassian.connect.play.java.auth.jwt;

import com.atlassian.jwt.Jwt;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Results;

import static com.google.common.base.Preconditions.checkNotNull;
import static play.libs.F.Either;
import static play.libs.F.Either.Left;
import static play.libs.F.Either.Right;
import static play.mvc.Results.Status;

/**
 * Contains the result of authenticating a Jwt secured request. The result is either an authenticated Jwt object on success,
 * or a status on failure
 */
public class JwtAuthenticationResult {
    private Jwt authenticatedJwt;
    private Status errorStatus;

    public JwtAuthenticationResult(Jwt authenticatedJwt) {

        this.authenticatedJwt = checkNotNull(authenticatedJwt);
    }

    public JwtAuthenticationResult(Status errorStatus) {

        this.errorStatus = checkNotNull(errorStatus);
    }

    public Either<Status, Jwt> getResult() {
        return (Either<Status, Jwt>) (authenticatedJwt == null ? Left(errorStatus) : Right(authenticatedJwt));
    }
}
