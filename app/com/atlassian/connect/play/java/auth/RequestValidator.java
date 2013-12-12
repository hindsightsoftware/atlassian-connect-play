package com.atlassian.connect.play.java.auth;

public interface RequestValidator<R> {
    String validate(R request);
}
