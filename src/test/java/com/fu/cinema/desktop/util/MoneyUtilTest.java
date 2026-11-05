package com.fu.cinema.desktop.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyUtilTest {

    @Test
    @DisplayName("format: null returns empty string")
    void format_nullReturnsEmpty() {
        assertEquals("", MoneyUtil.format(null));
    }

    @Test
    @DisplayName("format: 1234000 returns formatted VNĐ string")
    void format_validNumber() {
        String result = MoneyUtil.format(new BigDecimal("1234000"));
        assertTrue(result.contains("1.234.000") && result.contains("đ"));
    }

    @Test
    @DisplayName("format: zero returns 0 đ")
    void format_zero() {
        String result = MoneyUtil.format(BigDecimal.ZERO);
        assertTrue(result.contains("0") && result.contains("đ"));
    }
}

