package com.andriusdgt.thedots.core.service;

import com.andriusdgt.thedots.core.model.Point;
import com.andriusdgt.thedots.core.model.Square;
import com.andriusdgt.thedots.core.repository.PointRepository;

import java.util.*;
import java.util.function.Function;

import static com.andriusdgt.thedots.core.model.SquareVertex.BOTTOM_RIGHT;
import static com.andriusdgt.thedots.core.model.SquareVertex.UPPER_RIGHT;
import static java.util.stream.Collectors.*;
import static org.paukov.combinatorics.CombinatoricsFactory.createSimpleCombinationGenerator;
import static org.paukov.combinatorics.CombinatoricsFactory.range;

public final class SquareService {

    private final PointRepository pointRepository;

    public SquareService(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    public List<Square> find(String listId) {
        Map<Integer, List<Point>> groupedXPoints = Collections.unmodifiableMap(
            pointRepository
                .findByListIdOrderByXAscYAsc(listId)
                .stream()
                .collect(groupingBy(Point::getX, toUnmodifiableList()))
        );

        return groupedXPoints
            .values()
            .stream()
            .map(toSquares(groupedXPoints))
            .flatMap(List::stream)
            .collect(toList());
    }

    private Function<List<Point>, List<Square>> toSquares(Map<Integer, List<Point>> groupedXPoints) {
        return pointGroup -> {
            List<Square> squares = new ArrayList<>();
            createSimpleCombinationGenerator(range(0, pointGroup.size() - 1), 2)
                .forEach(indexPair -> {
                    int firstIndex = indexPair.getValue(0);
                    int secondIndex = indexPair.getValue(1);
                    Square square = new Square(pointGroup.get(firstIndex), pointGroup.get(secondIndex));
                    if (squareExists(groupedXPoints, square, pointGroup.get(firstIndex).getX()))
                        squares.add(square);
                });
            return squares;
        };
    }

    private boolean squareExists(Map<Integer, List<Point>> groupedXPoints, Square square, int x) {
        return groupedXPoints
            .getOrDefault(x + square.getSideLength(), new ArrayList<>())
            .containsAll(new HashSet<>(Set.of(square.getVertex(UPPER_RIGHT), square.getVertex(BOTTOM_RIGHT))));
    }

}
