package com.andriusdgt.thedots.core.repository;

import com.andriusdgt.thedots.core.model.Point;

import java.util.List;

public interface PointRepository {

    void save(Point point);

    void saveAll(Iterable<Point> points);

    List<Point> findByListId(String listId);

    List<Point> findByListId(String listId, int pageIndex, int pageSize);

    boolean exists(Point point);

    long countByListId(String listId);

    void deleteByListId(String listId);

    void deleteById(String id);

}
