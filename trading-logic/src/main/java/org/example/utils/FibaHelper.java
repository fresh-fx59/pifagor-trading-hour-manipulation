package org.example.utils;

import org.example.model.enums.FibaLevel;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class FibaHelper {

    public static Map<FibaLevel, BigDecimal> calculateValueForLevel(
            BigDecimal low, BigDecimal high
    ) {
        Map<FibaLevel, BigDecimal> result = new LinkedHashMap<>();

        BigDecimal range = low.subtract(high);

        Arrays.stream(FibaLevel.values()).forEach(
                fibaLevel -> {
                    BigDecimal value = high.add(range.multiply(fibaLevel.getLevel()));
                    result.put(fibaLevel, value);
                }
        );

        return result;
    }

//    public static List<Order> getOrders(HashMap<FibaLevel, BigDecimal> fibaPriceLevels,
//                                        OrderSide orderSide,
//                                        OrderType orderType,
//                                        List<FibaLevel> levelsToCreateOrder) {
//        List<Order> result = new ArrayList<>();
//
//        fibaPriceLevels.forEach((level, price) -> {
//            if (levelsToCreateOrder.contains(level))
//                result.add(new Order(orderType, price, orderSide, null));
//        });
//
//        return result;
//    }
}
