package com.andriusdgt.thedots.core.repository;

import com.andriusdgt.thedots.core.model.PointList;

import java.util.List;

public interface PointListRepository {

    void save(PointList pointList);

    PointList findByName(String name);

    List<PointList> findAll();

    void delete(PointList pointList);

    void deleteById(String id);

}
