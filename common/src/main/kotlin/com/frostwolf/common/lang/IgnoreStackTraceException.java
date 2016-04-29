package com.frostwolf.common.lang;

public class IgnoreStackTraceException extends RuntimeException {

    public IgnoreStackTraceException() {
    }

    public IgnoreStackTraceException(String message) {
        super(message);
    }

    public IgnoreStackTraceException(String message, Throwable cause) {
        super(message, cause);
    }

    public IgnoreStackTraceException(Throwable cause) {
        super(cause);
    }

    public IgnoreStackTraceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
