package com.andriusdgt.thedots.core.model;

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
            UPPER_RIGHT, createRightVertex(UPPER_LEFT),
            BOTTOM_RIGHT, createRightVertex(BOTTOM_LEFT)
        ));
    }

    public Map<SquareVertex, Point> getVertices() {
        return vertices;
    }

    public Point getVertex(SquareVertex vertex) {
        return vertices.get(vertex);
    }

    public int getSideLength() {
        return Math.abs(getVertex(UPPER_LEFT).getY() - getVertex(BOTTOM_LEFT).getY());
    }

    private Point createRightVertex(SquareVertex leftVertex) {
        Point leftPoint = getVertex(leftVertex);
        return new Point(
            leftPoint.getX() + getSideLength(),
            leftPoint.getY(),
            leftPoint.getListId()
        );
    }
}
