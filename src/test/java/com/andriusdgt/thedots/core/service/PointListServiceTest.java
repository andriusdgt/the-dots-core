package com.andriusdgt.thedots.core.service;

import com.andriusdgt.thedots.core.model.PointList;
import com.andriusdgt.thedots.core.repository.PointListRepository;
import com.andriusdgt.thedots.core.repository.PointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointListServiceTest {

    @Mock
    private Validator validator;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private PointListRepository pointListRepository;

    private PointListService pointListService;

    @BeforeEach
    public void setUp() {
        pointListService = new PointListService(validator, pointRepository, pointListRepository);
    }

    @Test
    public void createsPointList() {
        PointList pointList = new PointList("listId", "list name");

        pointListService.create(pointList);

        verify(pointListRepository).save(pointList);
    }

    @Test
    public void doesNotCreateOnValidationError() {
        PointList pointList = new PointList("listId", "list name");
        @SuppressWarnings("unchecked")
        ConstraintViolation<String> constraintViolationStub = mock(ConstraintViolation.class);
        doReturn("validation error").when(constraintViolationStub).getMessage();
        doReturn(Set.of(constraintViolationStub)).when(validator).validate(pointList);

        Exception ex = assertThrows(ValidationException.class, () -> pointListService.create(pointList));

        assertEquals("validation error", ex.getMessage());
        verify(pointListRepository, never()).save(pointList);
    }

    @Test
    public void deletedPointListOfDuplicateName() {
        PointList pointListToSave = new PointList("newListId", "list name");
        PointList savedPointList = new PointList("listId", "list name");
        doReturn(savedPointList).when(pointListRepository).findByName("list name");

        pointListService.create(pointListToSave);

        verify(pointListRepository).delete(savedPointList);
    }

    @Test
    public void deletedPointsFromListOfDuplicateName() {
        PointList pointListToSave = new PointList("newListId", "list name");
        PointList savedPointList = new PointList("listId", "list name");
        doReturn(savedPointList).when(pointListRepository).findByName("list name");

        pointListService.create(pointListToSave);

        verify(pointRepository).deleteByListId(savedPointList.getId());
    }

}
