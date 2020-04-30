package com.andriusdgt.thedots.core.service;

import com.andriusdgt.thedots.core.model.*;
import com.andriusdgt.thedots.core.repository.PointListRepository;
import com.andriusdgt.thedots.core.repository.PointRepository;

import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.andriusdgt.thedots.core.model.SquareVertex.BOTTOM_RIGHT;
import static com.andriusdgt.thedots.core.model.SquareVertex.UPPER_RIGHT;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.paukov.combinatorics.CombinatoricsFactory.*;

public final class PointListService {

    private final Validator validator;
    private final PointRepository pointRepository;
    private final PointListRepository pointListRepository;

    private static final String INCORRECT_FORMAT_WARNING = "Found incorrectly formatted lines, ignoring";
    private static final String DUPLICATES_FOUND_WARNING = "Found duplicates, only distinct ones will be preserved";
    private static final String LIST_SIZE_EXCEED_WARNING =
        "New points exceeds list size limit of %d, not all points will be imported";
    private static final String POINTS_LINE_REGEX = "[-]?\\d+ [-]?\\d+";

    public PointListService(
        Validator validator,
        PointRepository pointRepository,
        PointListRepository pointListRepository
    ) {
        this.validator = validator;
        this.pointRepository = pointRepository;
        this.pointListRepository = pointListRepository;
    }

    public void create(PointList pointList) {
        if (!validator.validate(pointList).isEmpty())
            throw new ValidationException(validator.validate(pointList).iterator().next().getMessage());

        PointList pointListOfSameName = pointListRepository.findByName(pointList.getName());
        if (isFound(pointListOfSameName))
            pointListRepository.delete(pointListOfSameName);
        if (isFound(pointListOfSameName) && !Objects.equals(pointListOfSameName.getId(), pointList.getId()))
            pointRepository.deleteByListId(pointListOfSameName.getId());

        pointListRepository.save(pointList);
    }

    public Set<Warning> create(Stream<String> linesStream, String listId, long pointListSizeLimit) {
        Set<Warning> warnings = new HashSet<>();
        List<Point> points = linesStream
            .peek(addFormatWarningIfPresent(warnings))
            .filter(line -> line.matches(POINTS_LINE_REGEX))
            .map(line -> createPoint(listId, line))
            .peek(addValidationWarningIfPresent(warnings))
            .filter(point -> validator.validate(point).isEmpty())
            .collect(Collectors.toList());

        int pointCount = points.size();

        points = points.stream().distinct().collect(Collectors.toList());
        List<Point> existingPoints = pointRepository.findByListId(listId);
        points.removeAll(existingPoints);
        if (points.size() != pointCount)
            warnings.add(new Warning(DUPLICATES_FOUND_WARNING));

        if (points.size() + existingPoints.size() > pointListSizeLimit) {
            warnings.add(new Warning(String.format(LIST_SIZE_EXCEED_WARNING, pointListSizeLimit)));
            points = points.subList(0, (int) pointListSizeLimit - existingPoints.size());
        }

        pointRepository.saveAll(points);
        return warnings;
    }

    public List<Square> findSquares(String listId) {
        SortedSet<Point> points = new TreeSet<>(pointRepository.findByListId(listId));
        Map<Integer, List<Point>> groupedXPoints = Collections.unmodifiableMap(
            points.stream().collect(Collectors.groupingBy(Point::getX, toUnmodifiableList()))
        );

        return groupedXPoints
            .values()
            .stream()
            .map(toSquares(groupedXPoints))
            .flatMap(List::stream)
            .collect(Collectors.toList());

    }

    private Consumer<Point> addValidationWarningIfPresent(Set<Warning> warnings) {
        return point -> {
            if (!validator.validate(point).isEmpty())
                warnings.add(new Warning(validator.validate(point).iterator().next().getMessage()));
        };
    }

    private Consumer<String> addFormatWarningIfPresent(Set<Warning> warnings) {
        return line -> {
            if (!line.matches(POINTS_LINE_REGEX))
                warnings.add(new Warning(INCORRECT_FORMAT_WARNING));
        };
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
        return getPoints(x + square.getSideLength(), groupedXPoints).containsAll(getRightVertices(square));
    }

    private List<Point> getPoints(int x, Map<Integer, List<Point>> groupedXPoints) {
        return groupedXPoints.getOrDefault(x, new ArrayList<>());
    }

    private HashSet<Point> getRightVertices(Square square) {
        return new HashSet<>(Set.of(square.getVertex(UPPER_RIGHT), square.getVertex(BOTTOM_RIGHT)));
    }

    private boolean isFound(PointList pointList) {
        return pointList != null;
    }

    private Point createPoint(String listId, String pointsLine) {
        int x = Integer.parseInt(pointsLine.split(" ")[0]);
        int y = Integer.parseInt(pointsLine.split(" ")[1]);
        return new Point(x, y, listId);
    }

    public String getPoints(String listId) {
        return pointRepository
            .findByListId(listId)
            .stream()
            .map(point -> String.format("%d %d", point.getX(), point.getY()))
            .collect(Collectors.joining("\n"));
    }
}
