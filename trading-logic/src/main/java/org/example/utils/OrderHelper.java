package org.example.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import static java.math.RoundingMode.DOWN;
import static java.math.RoundingMode.HALF_UP;

public class OrderHelper {

    /**
     * Generate UUID with specific size
     *
     * @param size of UUID
     * @return UUID
     */
    public static String generateUUID(int size) {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString().replace("-", "");
        return uuidString.substring(0, size);
    }

    /**
     * Get plain string big decimal rounded to specific value
     *
     * @param incomingPrice to process
     * @param roundSignQuantity signs after dot
     * @return string representation of big decimal
     */
    public static String roundBigDecimalHalfUp(BigDecimal incomingPrice, Integer roundSignQuantity) {
        return roundBigDecimal(incomingPrice, roundSignQuantity, HALF_UP);
    }

    public static String roundBigDecimalDown(BigDecimal incomingPrice, Integer roundSignQuantity) {
        return roundBigDecimal(incomingPrice, roundSignQuantity, DOWN);
    }

    private static String roundBigDecimal(BigDecimal incomingPrice, Integer roundSignQuantity, RoundingMode roundingMode) {
        return incomingPrice.setScale(roundSignQuantity, roundingMode).toPlainString();
    }
}