package com.andriusdgt.thedots.core.exception;

public class DuplicatePointException extends RuntimeException {

    public DuplicatePointException() {
        super("Duplicate Point provided");
    }

}
