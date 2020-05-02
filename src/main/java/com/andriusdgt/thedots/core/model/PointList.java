package com.andriusdgt.thedots.core.model;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public final class PointList implements Serializable {

    private String id;

    @NotNull(message = "{com.andriusdgt.thedots.core.model.PointList.name.NotNull.message}")
    private String name;

    public PointList() {
    }

    public PointList(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
