package com.andriusdgt.thedots.core.service;

import com.andriusdgt.thedots.core.model.Point;
import com.andriusdgt.thedots.core.model.PointList;
import com.andriusdgt.thedots.core.model.Warning;
import com.andriusdgt.thedots.core.repository.PointListRepository;
import com.andriusdgt.thedots.core.repository.PointRepository;

import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

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
            .map(line -> createPoint(line, listId))
            .peek(addValidationWarningIfPresent(warnings))
            .filter(point -> validator.validate(point).isEmpty())
            .collect(toList());

        int pointCount = points.size();

        points = points.stream().distinct().collect(toList());
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

    private boolean isFound(PointList pointList) {
        return pointList != null;
    }

    private Point createPoint(String pointsLine, String listId) {
        int x = Integer.parseInt(pointsLine.split(" ")[0]);
        int y = Integer.parseInt(pointsLine.split(" ")[1]);
        return new Point(x, y, listId);
    }

    public String getPoints(String listId) {
        return pointRepository
            .findByListId(listId)
            .stream()
            .map(point -> String.format("%d %d", point.getX(), point.getY()))
            .collect(joining("\n"));
    }

}
