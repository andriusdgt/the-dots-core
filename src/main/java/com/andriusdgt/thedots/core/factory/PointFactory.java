package com.andriusdgt.thedots.core.factory;

import com.andriusdgt.thedots.core.model.Point;

public final class PointFactory {

    public static Point from(String pointsLine, String listId) {
        int x = Integer.parseInt(pointsLine.split(" ")[0]);
        int y = Integer.parseInt(pointsLine.split(" ")[1]);
        return new Point(x, y, listId);
    }

}
