package com.github.rudder.shared.http;

public class MethodCallFailedException extends RuntimeException {

    public MethodCallFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
