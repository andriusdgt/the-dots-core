package com.andriusdgt.thedots.core.service;

import com.andriusdgt.thedots.core.exception.DuplicatePointException;
import com.andriusdgt.thedots.core.exception.TooManyPointsException;
import com.andriusdgt.thedots.core.model.Point;
import com.andriusdgt.thedots.core.repository.PointRepository;

import javax.validation.ValidationException;
import javax.validation.Validator;

public final class PointService {

    private final Validator validator;
    private final PointRepository pointRepository;

    public PointService(Validator validator, PointRepository pointRepository) {
        this.validator = validator;
        this.pointRepository = pointRepository;
    }

    public void create(Point point, long pointListSizeLimit) {
        if (!validator.validate(point).isEmpty())
            throw new ValidationException(validator.validate(point).iterator().next().getMessage());

        if (pointRepository.exists(point))
            throw new DuplicatePointException();

        long pointCount = pointRepository.countByListId(point.getListId());
        if (pointCount + 1 > pointListSizeLimit)
            throw new TooManyPointsException(pointCount);

        pointRepository.save(point);
    }

}
