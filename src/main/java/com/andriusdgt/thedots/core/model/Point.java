package com.andriusdgt.thedots.core.model;

import com.andriusdgt.thedots.core.annotation.Range;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public final class Point implements Serializable, Comparable<Point> {

    private String id;

    @Range(min = -5000, max = 5000)
    private short x;

    @Range(min = -5000, max = 5000)
    private short y;

    @NotNull(message = "{com.andriusdgt.thedots.core.model.Point.listId.NotNull.message}")
    private String listId;

    public Point() {
    }

    public Point(short x, short y, String listId) {
        this.x = x;
        this.y = y;
        this.listId = listId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;

        Point that = (Point) o;

        if (getX() != that.getX()) return false;
        if (getY() != that.getY()) return false;
        return getListId().equals(that.getListId());
    }

    @Override
    public int hashCode() {
        int result = getX();
        result = 31 * result + (int) getY();
        result = 31 * result + getListId().hashCode();
        return result;
    }

    @Override
    public int compareTo(Point other) {
        int result = Short.compare(this.x, other.x);
        if (result == 0)
            result = Short.compare(this.y, other.y);
        return result;
    }

    public String getId() {
        return id;
    }

    public short getX() {
        return x;
    }

    public short getY() {
        return y;
    }

    public String getListId() {
        return listId;
    }
}
