package com.example.demo.count;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LogicTest {

    @Test
    void countTotal_normalCase() {
        assertEquals(350.0, Logic.countTotal(350.0, 1));
    }

    @Test
    void countTotal_multipleQuantity() {
        assertEquals(700.0, Logic.countTotal(350.0, 2));
    }

    @Test
    void countTotal_zeroQuantity() {
        assertEquals(0.0, Logic.countTotal(350.0, 0));
    }

    @Test
    void countTotal_zeroPrice() {
        assertEquals(0.0, Logic.countTotal(0.0, 5));
    }

    @Test
    void countTotal_decimalPrice() {
        assertEquals(110.5, Logic.countTotal(55.25, 2), 0.001);
    }
}
