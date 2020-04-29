package com.andriusdgt.thedots.core.exception;

public final class TooManyPointsException extends RuntimeException {

    public TooManyPointsException(long threshold) {
        super("Too many points found in List, should not exceed the size of " + threshold);
    }

}
