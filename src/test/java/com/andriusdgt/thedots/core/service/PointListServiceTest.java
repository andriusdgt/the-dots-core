package com.andriusdgt.thedots.core.service;

import com.andriusdgt.thedots.core.model.*;
import com.andriusdgt.thedots.core.repository.PointListRepository;
import com.andriusdgt.thedots.core.repository.PointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
final class PointListServiceTest {

    @Mock
    private Validator validator;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private PointListRepository pointListRepository;

    private PointListService pointListService;

    @BeforeEach
    void setUp() {
        pointListService = new PointListService(validator, pointRepository, pointListRepository);
    }

    @Test
    void createsPointList() {
        PointList pointList = new PointList(null, "list name");

        pointListService.create(pointList);

        verify(pointListRepository).save(pointList);
    }

    @Test
    void doesNotCreateOnValidationError() {
        PointList pointList = new PointList(null, "list name");
        @SuppressWarnings("unchecked")
        ConstraintViolation<String> constraintViolationStub = mock(ConstraintViolation.class);
        doReturn("validation error").when(constraintViolationStub).getMessage();
        doReturn(Set.of(constraintViolationStub)).when(validator).validate(pointList);

        Exception ex = assertThrows(ValidationException.class, () -> pointListService.create(pointList));

        assertEquals("validation error", ex.getMessage());
        verify(pointListRepository, never()).save(pointList);
    }

    @Test
    void deletedPointListOfDuplicateName() {
        PointList pointListToSave = new PointList(null, "list name");
        PointList savedPointList = new PointList("listId", "list name");
        doReturn(savedPointList).when(pointListRepository).findByName("list name");

        pointListService.create(pointListToSave);

        verify(pointListRepository).delete(savedPointList);
    }

    @Test
    void deletedPointsFromListOfDuplicateName() {
        PointList pointListToSave = new PointList(null, "list name");
        PointList savedPointList = new PointList("listId", "list name");
        doReturn(savedPointList).when(pointListRepository).findByName("list name");

        pointListService.create(pointListToSave);

        verify(pointRepository).deleteByListId(savedPointList.getId());
    }

    @Nested
    class PointsImporting {

        @Test
        void savesImportedPoints() {
            pointListService.create(Stream.of("10 10", "-10 -20"), "listId", 100);

            verify(pointRepository).saveAll(Arrays.asList(new Point(10, 10, "listId"), new Point(-10, -20, "listId")));
        }

        @Test
        void emptyStreamSavesNoPoints() {
            pointListService.create(Stream.empty(), "listId", 100);

            verify(pointRepository).saveAll(new ArrayList<>());
        }

        @Test
        void emptyStreamReturnsNoWarnings() {
            Set<Warning> warnings = pointListService.create(Stream.empty(), "listId", 100);

            assertTrue(warnings.isEmpty());
        }

        @Test
        void ignoresLinesWithMissingPoints() {
            Stream<String> lines = Stream.of("10 20", "10 ", "10 ", "");

            pointListService.create(lines, "listId", 100);

            verify(pointRepository).saveAll(Collections.singletonList(new Point(10, 20, "listId")));
        }

        @Test
        void ignoresIncorrectlyFormattedLines() {
            Stream<String> lines = Stream.of("10 20", "heyy :)", "10 30 10", "--10 20", "10 --20", "-10 -", "- -10",
                "10 10\n10 30", "10 10oops", "10 10 ", " 10 10", "10  10", "10.0 10.0", "10,10");

            pointListService.create(lines, "listId", 100);

            verify(pointRepository).saveAll(Collections.singletonList(new Point(10, 20, "listId")));
        }

        @Test
        void producesIncorrectFormatWarning() {
            Set<Warning> warnings = pointListService.create(Stream.of("bananas"), "listId", 100);

            assertEquals(1, warnings.size());
            assertTrue(warnings.iterator().next().getMessage().toLowerCase().contains("incorrectly formatted"));
        }

        @Test
        void ignoresLinesWithValidationViolations() {
            Point point = new Point(10, 20, "listId");
            Point invalidPoint = new Point(10, -999999, "listId");
            @SuppressWarnings("unchecked")
            ConstraintViolation<String> constraintViolationStub = mock(ConstraintViolation.class);
            doReturn(new HashSet<>()).when(validator).validate(point);
            doReturn(Set.of(constraintViolationStub)).when(validator).validate(invalidPoint);

            pointListService.create(Stream.of("10 20", "10 -999999"), "listId", 100);

            verify(pointRepository).saveAll(Collections.singletonList(new Point(10, 20, "listId")));
        }

        @Test
        void producesValidationWarning() {
            Point point = new Point(10, 20, "listId");
            Point invalidPoint = new Point(10, -999999, "listId");
            @SuppressWarnings("unchecked")
            ConstraintViolation<String> constraintViolationStub = mock(ConstraintViolation.class);
            doReturn("validation error").when(constraintViolationStub).getMessage();
            doReturn(new HashSet<>()).when(validator).validate(point);
            doReturn(Set.of(constraintViolationStub)).when(validator).validate(invalidPoint);

            Set<Warning> warnings = pointListService.create(Stream.of("10 20", "10 -999999"), "listId", 100);

            assertEquals(1, warnings.size());
            assertTrue(warnings.iterator().next().getMessage().contains("validation error"));
        }

        @Test
        void onlyOneWarningOfSameTypeIsProduced() {
            Point invalidPoint = new Point(10, -999999, "listId");
            @SuppressWarnings("unchecked")
            ConstraintViolation<String> constraintViolationStub = mock(ConstraintViolation.class);
            doReturn("validation error").when(constraintViolationStub).getMessage();
            doReturn(Set.of(constraintViolationStub)).when(validator).validate(invalidPoint);

            Set<Warning> warnings = pointListService.create(Stream.of("10 -999999", "10 -999999"), "listId", 100);

            assertEquals(1, warnings.size());
        }

        @Test
        void doesNotSaveAlreadySavedPoints() {
            doReturn(Collections.singletonList(new Point(5, 5, "listId"))).when(pointRepository).findByListId("listId");

            pointListService.create(Stream.of("5 5"), "listId", 100);

            verify(pointRepository).saveAll(new ArrayList<>());
        }

        @Test
        void producesSavedDuplicatesFoundWarning() {
            doReturn(Collections.singletonList(new Point(5, 5, "listId"))).when(pointRepository).findByListId("listId");

            Set<Warning> warnings = pointListService.create(Stream.of("5 5"), "listId", 100);

            assertEquals(1, warnings.size());
            assertTrue(warnings.iterator().next().getMessage().toLowerCase().contains("duplicates"));
        }

        @Test
        void doesNotSaveDuplicatedPoints() {
            pointListService.create(Stream.of("5 5", "5 5"), "listId", 100);

            verify(pointRepository).saveAll(Collections.singletonList(new Point(5, 5, "listId")));
        }

        @Test
        void producesDuplicatesFoundWarning() {
            Set<Warning> warnings = pointListService.create(Stream.of("5 5", "5 5"), "listId", 100);

            assertEquals(1, warnings.size());
            assertTrue(warnings.iterator().next().getMessage().toLowerCase().contains("duplicates"));
        }

        @Test
        void doesNotSaveTooManyPointsAtTheTime() {
            pointListService.create(Stream.of("10 20", "-10 -20"), "listId", 1);

            verify(pointRepository).saveAll(Collections.singletonList(new Point(10, 20, "listId")));
        }

        @Test
        void doesNotSaveTooManyPointsWhenListContainsSomePoints() {
            doReturn(Collections.singletonList(new Point(5, 5, "listId"))).when(pointRepository).findByListId("listId");

            pointListService.create(Stream.of("10 20", "-10 -20"), "listId", 2);

            verify(pointRepository).saveAll(Collections.singletonList(new Point(10, 20, "listId")));
        }

        @Test
        void producesSizeLimitReachedWarning() {
            Set<Warning> warnings = pointListService.create(Stream.of("10 20", "-10 -20"), "listId", 1);

            assertEquals(1, warnings.size());
            assertTrue(warnings.iterator().next().getMessage().toLowerCase().contains("size limit of " + 1));
        }
    }

    @Test
    void getsPointFromList() {
        List<Point> points = Collections.singletonList(new Point(10, -20, "listId"));
        doReturn(points).when(pointRepository).findByListId("listId");

        assertEquals("10 -20", pointListService.getPoints("listId"));
    }

    @Test
    void getsPointsFromList() {
        List<Point> points = Arrays.asList(new Point(10, -20, "listId"), new Point(-10, 20, "listId"));
        doReturn(points).when(pointRepository).findByListId("listId");

        assertEquals("10 -20\n-10 20", pointListService.getPoints("listId"));
    }

    @Test
    void getsPointsFromEmptyList() {
        assertEquals("", pointListService.getPoints("listId"));
    }

}
