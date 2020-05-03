package com.andriusdgt.thedots.core.model;

import com.andriusdgt.thedots.core.annotation.Range;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public final class Point implements Serializable {

    private String id;

    @Range(min = -5000, max = 5000)
    private int x;

    @Range(min = -5000, max = 5000)
    private int y;

    @NotNull(message = "{com.andriusdgt.thedots.core.model.Point.listId.NotNull.message}")
    private String listId;

    public Point() {
    }

    public Point(int x, int y, String listId) {
        this.x = x;
        this.y = y;
        this.listId = listId;
    }

    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getListId() {
        return listId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;

        Point point = (Point) o;

        if (getX() != point.getX()) return false;
        if (getY() != point.getY()) return false;
        return getListId().equals(point.getListId());
    }

    @Override
    public int hashCode() {
        int result = getX();
        result = 31 * result + getY();
        result = 31 * result + getListId().hashCode();
        return result;
    }
}
