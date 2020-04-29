package com.andriusdgt.thedots.core.service;

import com.andriusdgt.thedots.core.model.Point;
import com.andriusdgt.thedots.core.model.PointList;
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

        PointList pointListWithDuplicateName = pointListRepository.findByName(pointList.getName());
        if (pointListWithDuplicateName != null && !Objects.equals(pointListWithDuplicateName.getId(), pointList.getId()))
            pointRepository.deleteByListId(pointListWithDuplicateName.getId());
        if (pointListWithDuplicateName != null)
            pointListRepository.delete(pointListWithDuplicateName);
        if (pointList.getId() != null)
            pointListRepository
                .findById(pointList.getId())
                .ifPresent(pointListRepository::delete);
        pointListRepository.save(pointList);
    }

    public Set<String> create(Stream<String> linesInputStream, String listId, long pointListSizeLimit) {
        Set<String> errors = new HashSet<>();
        List<Point> points = linesInputStream
            .peek(line -> {
                if (!line.matches("[-]?\\d+ [-]?\\d+"))
                    errors.add("Found incorrectly formatted lines, ignoring");
            })
            .filter(line -> line.matches("[-]?\\d+ [-]?\\d+"))
            .map(line -> new AbstractMap.SimpleEntry<>(line.split(" ")[0], line.split(" ")[1]))
            .map(pair -> new Point(Short.parseShort(pair.getKey()), Short.parseShort(pair.getValue()), listId))
            .peek(point -> {
                if (!validator.validate(point).isEmpty())
                    errors.add(validator.validate(point).iterator().next().getMessage());
            })
            .filter(point -> validator.validate(point).isEmpty())
            .collect(Collectors.toList());

        int pointCount = points.size();

        points = points.stream().distinct().collect(Collectors.toList());
        List<Point> existingPoints = pointRepository.findByListId(listId);
        points.removeAll(existingPoints);
        if (points.size() != pointCount)
            errors.add("Found duplicates, only distinct ones will be preserved");

        if (points.size() + existingPoints.size() > pointListSizeLimit) {
            errors.add("New points exceeds list size limit of " + pointListSizeLimit + ", not all points will be imported");
            points = points.subList(0, (int) pointListSizeLimit - existingPoints.size());
        }

        pointRepository.saveAll(points);
        return errors;
    }

    public List<List<Point>> findSquares(String listId) {
        List<List<Point>> squares = new ArrayList<>();
        SortedSet<Point> points = new TreeSet<>(pointRepository.findByListId(listId));
        Map<Short, List<Point>> xAxisPoints =
            points.stream().collect(Collectors.groupingBy(Point::getX, toList()));

        //noinspection SimplifyStreamApiCallChains
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
