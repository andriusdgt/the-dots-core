package com.andriusdgt.thedots.core.model;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public final class PointList implements Serializable {

    private String id;

    @NotNull(message = "{com.andriusdgt.thedots.api.model.PointList.name.NotNull.message}")
    private String name;

    public PointList() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
