package org.example.utils;

import lombok.NoArgsConstructor;
import org.example.model.Order;
import org.example.model.enums.FibaLevel;
import org.example.model.enums.OrderSide;
import org.example.model.enums.OrderType;

import java.math.BigDecimal;
import java.util.*;

public class FibaActions {

    public static Map<FibaLevel, BigDecimal> calculateValueForLevel(
            BigDecimal low, BigDecimal high
    ) {
        Map<FibaLevel, BigDecimal> result = new LinkedHashMap<>();

        BigDecimal range = high.subtract(low);

        Arrays.stream(FibaLevel.values()).forEach(
                fibaLevel -> {
                    BigDecimal value = low.add(range.multiply(fibaLevel.getLevel()));
                    result.put(fibaLevel, value);
                }
        );

        return result;
    }

    public static List<Order> getOrders(HashMap<FibaLevel, BigDecimal> fibaPriceLevels,
                                        OrderSide orderSide,
                                        OrderType orderType,
                                        List<FibaLevel> levelsToCreateOrder) {
        List<Order> result = new ArrayList<>();

        fibaPriceLevels.forEach((level, price) -> {
            if (levelsToCreateOrder.contains(level))
                result.add(new Order(orderType, price, orderSide, null));
        });

        return result;
    }
}
