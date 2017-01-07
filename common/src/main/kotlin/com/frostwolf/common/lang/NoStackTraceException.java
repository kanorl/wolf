package com.frostwolf.common.lang;

public class NoStackTraceException extends RuntimeException {

    public NoStackTraceException() {
    }

    public NoStackTraceException(String message) {
        super(message);
    }

    public NoStackTraceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoStackTraceException(Throwable cause) {
        super(cause);
    }

    public NoStackTraceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}