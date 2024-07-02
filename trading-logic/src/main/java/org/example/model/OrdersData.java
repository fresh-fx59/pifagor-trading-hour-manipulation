package org.example.model;

import org.example.model.enums.FibaLevel;
import org.example.model.enums.OrdersDataParams;

import java.math.BigDecimal;
import java.util.Map;

import static org.example.service.KlineCandleProcessorImpl.ROUND_SIGN_QUANTITY;
import static org.example.utils.OrderHelper.getPrice;

public record OrdersData(Map<FibaLevel, Order> levelOrder,
                         Map<FibaLevel, BigDecimal> fibaLevelsToCompare,
                         Map<OrdersDataParams, Boolean> params) {
    /**
     * max prefix size 15
     */
    public static final String STOP_LOSS_PREFIX = "stopLoss-";
    public static final String BUY_PREFIX = "buy-";
    public static final String TAKE_PROFIT_PREFIX = "takeProfit-";

    public void cleanUp() {
        levelOrder.clear();
        fibaLevelsToCompare.clear();
        params.clear();
    }

    /**
     * Amend order if it exists or create new one from provided.
     * @param newLevelPrice prices to update
     * @param ordersToCreate orders to add if there is no orders
     * @return map of edited orders
     */
    public Map<FibaLevel, Order> amendOrders(Map<FibaLevel, BigDecimal> newLevelPrice,
                                             Map<FibaLevel, Order> ordersToCreate) {
        newLevelPrice.forEach((level, price) -> {
            Order savedOrder = levelOrder.get(level);
            if (savedOrder != null) {
                savedOrder.setPrice(getPrice(price, ROUND_SIGN_QUANTITY));
            } else {
                levelOrder.put(level, ordersToCreate.get(level));
            }
        });
        fibaLevelsToCompare.putAll(newLevelPrice);

        return levelOrder;
    }

    public BigDecimal getFiba05() {
        return fibaLevelsToCompare.get(FibaLevel.FIVE);
    }

    public Boolean getParam(OrdersDataParams param, Boolean defaultValue) {
        Boolean paramValue = this.params.get(param);
        return paramValue == null ? defaultValue : paramValue;
    }

    public Boolean isNotFreezed() {
        return !getParam(OrdersDataParams.FREEZED, false);
    }

    public void updateParams(Map<OrdersDataParams, Boolean> newParams) {
        params.putAll(newParams);
    }

    public void updateLevelOrder(Map<FibaLevel, Order> newLevelOrder) {
        levelOrder.putAll(newLevelOrder);
    }

    public void updateFibaLevelsToCompare(Map<FibaLevel, BigDecimal> newFibaLevelsToCompare) {
        fibaLevelsToCompare.putAll(newFibaLevelsToCompare);
    }
}
