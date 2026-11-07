package com.fu.cinema.desktop.facade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CounterSaleFacadeTest {

    @Test
    @DisplayName("SEAT_ORDER sorts seats row-wise and numerically (A2 < A10 < B1)")
    void testSeatOrderComparator() {
        List<String> seats = Arrays.asList("B1", "A10", "A2", "B2", "A1");
        seats.sort(CounterSaleFacade.SEAT_ORDER);
        assertEquals(List.of("A1", "A2", "A10", "B1", "B2"), seats);
    }
}
