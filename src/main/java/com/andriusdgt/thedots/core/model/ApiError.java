package com.andriusdgt.thedots.core.model;

public final class ApiError {

    private final String message;

    public ApiError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
