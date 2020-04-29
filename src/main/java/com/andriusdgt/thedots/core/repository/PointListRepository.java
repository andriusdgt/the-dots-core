package com.andriusdgt.thedots.core.repository;

import com.andriusdgt.thedots.core.model.PointList;

import java.util.List;
import java.util.Optional;

public interface PointListRepository {

    void save(PointList pointList);

    Optional<PointList> findById(String id);

    PointList findByName(String name);

    List<PointList> findAll();

    void delete(PointList pointList);

    void deleteById(String id);

}
