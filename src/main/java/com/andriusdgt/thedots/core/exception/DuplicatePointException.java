package com.andriusdgt.thedots.core.exception;

public final class DuplicatePointException extends RuntimeException {

    public DuplicatePointException() {
        super("Duplicate Point provided");
    }

}
