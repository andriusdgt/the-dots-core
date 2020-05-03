package com.andriusdgt.thedots.core.factory;

import com.andriusdgt.thedots.core.model.Point;
import com.andriusdgt.thedots.core.model.Square;
import com.andriusdgt.thedots.core.model.SquareVertex;

import static com.andriusdgt.thedots.core.model.SquareVertex.BOTTOM_LEFT;
import static com.andriusdgt.thedots.core.model.SquareVertex.UPPER_LEFT;

public final class SquareFactory {

    public static Point newRightPoint(Square square, SquareVertex leftVertex) {
        Point leftPoint = getPoint(leftVertex, square);
        return new Point(
            leftPoint.getX() + getSideLength(square),
            leftPoint.getY(),
            leftPoint.getListId()
        );
    }

    public static Point getPoint(SquareVertex vertex, Square square) {
        return square.getVertices().get(vertex);
    }

    public static int getSideLength(Square sq) {
        return Math.abs(sq.getVertices().get(UPPER_LEFT).getY() - sq.getVertices().get(BOTTOM_LEFT).getY());
    }


}
