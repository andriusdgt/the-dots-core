package com.andriusdgt.thedots.core.service;

import com.andriusdgt.thedots.core.model.Point;
import com.andriusdgt.thedots.core.model.PointList;
import com.andriusdgt.thedots.core.model.Warning;
import com.andriusdgt.thedots.core.repository.PointListRepository;
import com.andriusdgt.thedots.core.repository.PointRepository;

import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class PointListService {

    private final Validator validator;
    private final PointRepository pointRepository;
    private final PointListRepository pointListRepository;

    private static final String INCORRECT_FORMAT_WARNING = "Found incorrectly formatted lines, ignoring";
    private static final String DUPLICATES_FOUND_WARNING = "Found duplicates, only distinct ones will be preserved";
    private static final String LIST_SIZE_EXCEED_WARNING_FORMAT =
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

    private boolean isFound(PointList pointList) {
        return pointList != null;
    }

    public Set<Warning> create(Stream<String> linesInputStream, String listId, long pointListSizeLimit) {
        Set<Warning> warnings = new HashSet<>();
        List<Point> points = linesInputStream
            .peek(line -> {
                if (!line.matches(POINTS_LINE_REGEX))
                    warnings.add(new Warning(INCORRECT_FORMAT_WARNING));
            })
            .filter(line -> line.matches(POINTS_LINE_REGEX))
            .map(line -> createPoint(listId, line))
            .peek(point -> {
                if (!validator.validate(point).isEmpty())
                    warnings.add(new Warning(validator.validate(point).iterator().next().getMessage()));
            })
            .filter(point -> validator.validate(point).isEmpty())
            .collect(Collectors.toList());

        int pointCount = points.size();

        points = points.stream().distinct().collect(Collectors.toList());
        List<Point> existingPoints = pointRepository.findByListId(listId);
        points.removeAll(existingPoints);
        if (points.size() != pointCount)
            warnings.add(new Warning(DUPLICATES_FOUND_WARNING));

        if (points.size() + existingPoints.size() > pointListSizeLimit) {
            warnings.add(new Warning(String.format(LIST_SIZE_EXCEED_WARNING_FORMAT, pointListSizeLimit)));
            points = points.subList(0, (int) pointListSizeLimit - existingPoints.size());
        }

        pointRepository.saveAll(points);
        return warnings;
    }

    private Point createPoint(String listId, String pointsLine) {
        short x = Short.parseShort(pointsLine.split(" ")[0]);
        short y = Short.parseShort(pointsLine.split(" ")[1]);
        return new Point(x, y, listId);
    }

    public String getPoints(String listId) {
        return pointRepository
            .findByListId(listId)
            .stream()
            .map(point -> String.format("%d %d", point.getX(), point.getY()))
            .collect(Collectors.joining("\n"));
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    public List<List<Point>> findSquares(String listId) {
        List<List<Point>> squares = new ArrayList<>();
        SortedSet<Point> points = new TreeSet<>(pointRepository.findByListId(listId));
        Map<Short, List<Point>> xAxisPoints =
            points.stream().collect(Collectors.groupingBy(Point::getX, toList()));

        xAxisPoints.values().stream().forEach(pointGroup -> {
            for (int firstPointIndex = 0; firstPointIndex < pointGroup.size() - 1; firstPointIndex++) {
                for (int secondPointIndex = firstPointIndex + 1; secondPointIndex < pointGroup.size(); secondPointIndex++) {
                    short x = pointGroup.get(firstPointIndex).getX();
                    short sideLength = (short) Math.abs(pointGroup.get(secondPointIndex).getY() - pointGroup.get(firstPointIndex).getY());
                    Point firstVertex = pointGroup.get(firstPointIndex);
                    Point secondVertex = pointGroup.get(secondPointIndex);
                    Point thirdVertex = new Point((short) (x + sideLength), firstVertex.getY(), listId);
                    Point fourthVertex = new Point((short) (x + sideLength), secondVertex.getY(), listId);
                    if (xAxisPoints.containsKey((short) (x + sideLength))
                        && xAxisPoints.get((short) (x + sideLength)).containsAll(new HashSet<>(Set.of(thirdVertex, fourthVertex))))
                        squares.add(Arrays.asList(firstVertex, secondVertex, thirdVertex, fourthVertex));
                }
            }
        });

        return squares;
    }
}
