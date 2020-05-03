package com.andriusdgt.thedots.core.model;

import com.andriusdgt.thedots.core.factory.SquareFactory;

import java.util.HashMap;
import java.util.Map;

import static com.andriusdgt.thedots.core.model.SquareVertex.*;

public final class Square {

    private Map<SquareVertex, Point> vertices;

    public Square() {
    }

    public Square(Point bottomLeftVertex, Point upperLeftVertex) {
        vertices = new HashMap<>(4);
        vertices.putAll(Map.of(
            BOTTOM_LEFT, bottomLeftVertex,
            UPPER_LEFT, upperLeftVertex
        ));
        vertices.putAll(Map.of(
            UPPER_RIGHT, SquareFactory.newRightPoint(this, UPPER_LEFT),
            BOTTOM_RIGHT, SquareFactory.newRightPoint(this, BOTTOM_LEFT)
        ));
    }

    public Map<SquareVertex, Point> getVertices() {
        return vertices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Square)) return false;

        Square square = (Square) o;

        return getVertices().equals(square.getVertices());
    }

    @Override
    public int hashCode() {
        return getVertices().hashCode();
    }

}
