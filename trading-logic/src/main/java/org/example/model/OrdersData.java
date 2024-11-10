package org.example.model;

import org.example.model.enums.FibaLevel;
import org.example.model.enums.OrdersDataParams;

import java.math.BigDecimal;
import java.util.Map;

import static org.example.model.enums.FibaLevel.*;
import static org.example.utils.OrderHelper.roundBigDecimalHalfUp;

public record OrdersData(
        Map<FibaLevel, BigDecimal> fibaLevelsToCompare,
        Map<OrdersDataParams, Boolean> params,
        Order order
) {
    public final static int ROUND_SIGN_PRICE = 3;
    public final static int ROUND_SIGN_QUANTITY = 3;
    /**
     * max prefix size 15
     */
    public static final String SLTP_PREFIX = "sltp-";

    public void cleanUp() {
        fibaLevelsToCompare.clear();
        params.clear();
        order.updateOrder(new Order());
    }

    public Boolean getParam(OrdersDataParams param, Boolean defaultValue) {
        Boolean paramValue = this.params.get(param);
        return paramValue == null ? defaultValue : paramValue;
    }

    public Boolean isNotFreezed() {
        return !getParam(OrdersDataParams.FREEZED, false);
    }

    public Boolean isFreezed() {
        return getParam(OrdersDataParams.FREEZED, false);
    }

    public void updateParams(Map<OrdersDataParams, Boolean> newParams) {
        params.putAll(newParams);
    }

    public void updateFibaLevelsToCompare(Map<FibaLevel, BigDecimal> newFibaLevelsToCompare) {
        fibaLevelsToCompare.putAll(newFibaLevelsToCompare);
    }

    public void copyOrder(Order incomingOrder) {
        order.updateOrder(incomingOrder);
    }

    public Order amendOrderPriceTpSl(Map<FibaLevel, BigDecimal> levelPrice) {
        order.setPrice(roundBigDecimalHalfUp(levelPrice.get(FIVE), ROUND_SIGN_PRICE));
        order.setStopLoss(roundBigDecimalHalfUp(levelPrice.get(ONE), ROUND_SIGN_PRICE));
        order.setTakeProfit(roundBigDecimalHalfUp(levelPrice.get(THREEEIGHTTWO), ROUND_SIGN_PRICE));
        fibaLevelsToCompare.putAll(levelPrice);
        return order;
    }
}
