package com.andriusdgt.thedots.core.model;

public final class Warning {

    private final String message;

    public Warning(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Warning)) return false;

        Warning warning = (Warning) o;

        return getMessage() != null ? getMessage().equals(warning.getMessage()) : warning.getMessage() == null;
    }

    @Override
    public int hashCode() {
        return getMessage() != null ? getMessage().hashCode() : 0;
    }

    public String getMessage() {
        return message;
    }

}
