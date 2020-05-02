package com.andriusdgt.thedots.core.service;

import com.andriusdgt.thedots.core.model.Point;
import com.andriusdgt.thedots.core.model.Square;
import com.andriusdgt.thedots.core.repository.PointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class SquareServiceTest {

    @Mock
    private PointRepository pointRepository;

    private SquareService squareService;

    @BeforeEach
    public void setUp() {
        squareService = new SquareService(pointRepository);
    }

    @Test
    public void findsASquare() {
        doReturn(
            Arrays.asList(
                new Point(0, 0, "listId"),
                new Point(0, 5, "listId"),
                new Point(2, 2, "listId"),
                new Point(5, 0, "listId"),
                new Point(5, 5, "listId")
            )
        ).when(pointRepository).findByListIdOrderByXAscYAsc("listId");

        List<Square> squares = squareService.find("listId");

        assertEquals(1, squares.size());
        assertEquals(new Square(new Point(0, 0, "listId"), new Point(0, 5, "listId")), squares.get(0));
    }

    @Test
    public void findsSeveralSquares() {
        doReturn(
            Arrays.asList(
                new Point(-20, -20, "listId"),
                new Point(-20, -18, "listId"),
                new Point(-18, -20, "listId"),
                new Point(-18, -18, "listId"),
                new Point(0, 0, "listId"),
                new Point(0, 5, "listId"),
                new Point(2, 2, "listId"),
                new Point(5, 0, "listId"),
                new Point(5, 5, "listId")
            )
        ).when(pointRepository).findByListIdOrderByXAscYAsc("listId");

        List<Square> squares = squareService.find("listId");

        assertEquals(2, squares.size());
        assertTrue(
            squares.containsAll(
                Arrays.asList(
                    new Square(new Point(-20, -20, "listId"), new Point(-20, -18, "listId")),
                    new Square(new Point(0, 0, "listId"), new Point(0, 5, "listId"))
                )
            )
        );
    }

    @Test
    public void findsSquaresWithSharedPoints() {
        doReturn(
            Arrays.asList(
                new Point(-20, -20, "listId"),
                new Point(-20, 0, "listId"),
                new Point(0, -20, "listId"),
                new Point(0, 0, "listId"),
                new Point(0, 20, "listId"),
                new Point(-20, 20, "listId"),
                new Point(-5, -5, "listId")
            )
        ).when(pointRepository).findByListIdOrderByXAscYAsc("listId");

        List<Square> squares = squareService.find("listId");

        assertEquals(2, squares.size());
        assertTrue(
            squares.containsAll(
                Arrays.asList(
                    new Square(new Point(-20, -20, "listId"), new Point(-20, 0, "listId")),
                    new Square(new Point(-20, 0, "listId"), new Point(-20, 20, "listId"))
                )
            )
        );
    }

    @Test
    public void findsSquaresInSquares() {
        doReturn(
            Arrays.asList(
                new Point(-20, -20, "listId"),
                new Point(-20, 0, "listId"),
                new Point(0, -20, "listId"),
                new Point(0, 0, "listId"),
                new Point(0, 20, "listId"),
                new Point(-20, 20, "listId"),
                new Point(20, 20, "listId"),
                new Point(20, 0, "listId"),
                new Point(20, -20, "listId")
            )
        ).when(pointRepository).findByListIdOrderByXAscYAsc("listId");

        List<Square> squares = squareService.find("listId");

        assertEquals(5, squares.size());
        assertTrue(
            squares.containsAll(
                Arrays.asList(
                    new Square(new Point(-20, -20, "listId"), new Point(-20, 0, "listId")),
                    new Square(new Point(-20, 0, "listId"), new Point(-20, 20, "listId")),
                    new Square(new Point(0, -20, "listId"), new Point(0, 0, "listId")),
                    new Square(new Point(0, 0, "listId"), new Point(0, 20, "listId")),
                    new Square(new Point(-20, -20, "listId"), new Point(-20, 20, "listId"))
                )
            )
        );
    }

    @Test
    public void ignoresRectangles() {
        doReturn(
            Arrays.asList(
                new Point(0, 0, "listId"),
                new Point(0, 3, "listId"),
                new Point(6, 0, "listId"),
                new Point(6, 3, "listId")
            )
        ).when(pointRepository).findByListIdOrderByXAscYAsc("listId");

        List<Square> squares = squareService.find("listId");

        assertEquals(0, squares.size());
    }

    @Test
    public void doesNotFindSquaresInEmptyList() {
        assertEquals(0, squareService.find("listId").size());
    }

    @Test
    public void doesNotFindSquaresFromIncompleteList() {
        doReturn(
            Arrays.asList(new Point(0, 0, "listId"), new Point(0, 5, "listId"), new Point(5, 5, "listId"))
        ).when(pointRepository).findByListIdOrderByXAscYAsc("listId");

        assertEquals(0, squareService.find("listId").size());
    }

}
