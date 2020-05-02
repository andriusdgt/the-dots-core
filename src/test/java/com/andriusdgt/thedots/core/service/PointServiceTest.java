package com.andriusdgt.thedots.core.service;

import com.andriusdgt.thedots.core.exception.DuplicatePointException;
import com.andriusdgt.thedots.core.exception.TooManyPointsException;
import com.andriusdgt.thedots.core.model.Point;
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
public class PointServiceTest {

    @Mock
    private Validator validator;

    @Mock
    private PointRepository pointRepository;

    private PointService pointService;

    @BeforeEach
    public void setUp() {
        pointService = new PointService(validator, pointRepository);
    }

    @Test
    public void createsPoint() {
        Point point = new Point(1, 1, "listId");
        doReturn(9999L).when(pointRepository).countByListId("listId");

        pointService.create(point, 10000L);

        verify(pointRepository).save(point);
    }

    @Test
    public void doesNotCreateOnValidationError() {
        Point point = new Point(1, 1, "listId");
        @SuppressWarnings("unchecked")
        ConstraintViolation<String> constraintViolationStub = mock(ConstraintViolation.class);
        doReturn("validation error").when(constraintViolationStub).getMessage();
        doReturn(Set.of(constraintViolationStub)).when(validator).validate(point);

        Exception ex = assertThrows(ValidationException.class, () -> pointService.create(point, 0));

        assertEquals("validation error", ex.getMessage());
        verify(pointRepository, never()).save(point);
    }

    @Test
    public void doesNotCreateDuplicate() {
        Point point = new Point(1, 1, "listId");
        doReturn(true).when(pointRepository).exists(point);

        assertThrows(DuplicatePointException.class, () -> pointService.create(point, 0));

        verify(pointRepository, never()).save(point);
    }

    @Test
    public void doesNotCreateWhenListIsFull() {
        Point point = new Point(1, 1, "listId");
        doReturn(10000L).when(pointRepository).countByListId("listId");

        assertThrows(TooManyPointsException.class, () -> pointService.create(point, 10000));

        verify(pointRepository, never()).save(point);
    }

}
