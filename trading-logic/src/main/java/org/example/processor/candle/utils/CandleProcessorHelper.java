package org.example.processor.candle.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.*;
import org.example.model.CandleEnvironment;
import org.example.model.Order;
import org.example.model.OrdersData;
import org.example.model.enums.FibaLevel;
import org.example.model.enums.OrdersDataParams;
import org.example.service.OrderService;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

import static org.example.enums.OrderStatus.FILLED;
import static org.example.model.OrdersData.*;
import static org.example.model.enums.FibaLevel.*;
import static org.example.utils.OrderHelper.*;

@Slf4j
public class CandleProcessorHelper {
    public static void cleanUpIfOrderWasFilled(OrderService orderService, OrdersData ordersData) {
        Order shouldBeFilledOrder = ordersData.order();
        OrderStatus orderStatus = orderService.getOrderStatus(shouldBeFilledOrder);
        if (FILLED.equals(orderStatus)) {
            ordersData.cleanUp();
        } else {
            log.error("Order ID {} status = {}, but should be Filled",
                    shouldBeFilledOrder.getOrderId(), orderStatus.getBybitStatus());
        }
    }

    public static Order prepareCreateOrder(
            Ticker ticker,
            Map<FibaLevel, BigDecimal> levelPrice,
            BigDecimal quantityThreshold,
            BigDecimal balance
    ) {
        BigDecimal orderPrice = levelPrice.get(FIVE);
        BigDecimal quantityWOThreshold = balance.divide(orderPrice, MathContext.DECIMAL32);
        BigDecimal quantity = quantityWOThreshold
                .subtract(quantityWOThreshold.multiply(quantityThreshold));
        return Order.builder()
                .category(OrderCategory.LINEAR)
                .ticker(ticker)
                .orderSide(OrderSide.BUY)
                .type(OrderType.LIMIT)
                .quantity(roundBigDecimalDown(quantity, ROUND_SIGN_QUANTITY))
                .price(roundBigDecimalHalfUp(orderPrice, ROUND_SIGN_PRICE))
                .takeProfit(roundBigDecimalHalfUp(levelPrice.get(THREEEIGHTTWO), ROUND_SIGN_PRICE))
                .stopLoss(roundBigDecimalHalfUp(levelPrice.get(ONE), ROUND_SIGN_PRICE))
                .customOrderId(SLTP_PREFIX + generateUUID21())
                .build();
    }

    /**
     * Generate UUID for orderLinkId(36 symbols max)
     * @return UUID
     */
    public static String generateUUID21() {
        return generateUUID(21);
    }

    public static void updateOrderData(
            Order order,
            Map<FibaLevel, BigDecimal> levelPrice,
            Map<OrdersDataParams, Boolean> params,
            OrdersData ordersData
    ) {
        ordersData.updateFibaLevelsToCompare(levelPrice);
        ordersData.copyOrder(order);
        ordersData.updateParams(params);
    }

    public static boolean isOrderFreezed(CandleEnvironment ce) {
        return ce.isOrderData05Exists()
                && ce.isIncomingCanldeLowLowerOrEqualOrderData05();
    }

    public static boolean isOrderAmended(CandleEnvironment ce) {
        return ce.isOrderData05Exists()
                && ce.isFiba05GreaterOrdersData05();
    }

    public static boolean isOrderFilledStopLoss(CandleEnvironment ce) {
        return ce.isCandleLowLowerOrEqualOrder1();
    }

    public static boolean isOrderFilledTakeProfit(CandleEnvironment ce) {
        return ce.isIncomingCandleHighHigherOrEqualOrderData0382();
    }

    public static boolean shouldPlaceOrder(CandleEnvironment ce) {
        return  ce.isMoreThan2HourCandles()
//                    && isLastMinuteOfHour(candlesTime)
//                    && incomingCandle.getIsKlineClosed()
                ;
    }
}
