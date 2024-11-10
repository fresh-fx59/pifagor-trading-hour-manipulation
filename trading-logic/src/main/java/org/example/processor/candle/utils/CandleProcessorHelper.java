package org.example.processor.candle.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.OrderCategory;
import org.example.enums.OrderSide;
import org.example.enums.OrderStatus;
import org.example.enums.OrderType;
import org.example.enums.Ticker;
import org.example.model.CandleEnvironment;
import org.example.model.Order;
import org.example.model.OrdersData;
import org.example.model.enums.FibaLevel;
import org.example.model.enums.OrdersDataParams;
import org.example.service.OrderService;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;
import java.util.Objects;

import static java.math.RoundingMode.DOWN;
import static org.example.enums.OrderStatus.FILLED;
import static org.example.model.OrdersData.ROUND_SIGN_PRICE;
import static org.example.model.OrdersData.ROUND_SIGN_QUANTITY;
import static org.example.model.OrdersData.SLTP_PREFIX;
import static org.example.model.enums.FibaLevel.FIVE;
import static org.example.model.enums.FibaLevel.ONE;
import static org.example.model.enums.FibaLevel.THREEEIGHTTWO;
import static org.example.utils.OrderHelper.generateUUID;
import static org.example.utils.OrderHelper.roundBigDecimalDown;
import static org.example.utils.OrderHelper.roundBigDecimalHalfUp;

@Slf4j
public class CandleProcessorHelper {
    /**
     * Calculate max possible leverage by (fiba05/(fiba05-fiba1))*risk/100 no more than max leverage. Return 1 if maxLeverage is 1
     *
     * @param percentOfDepositToLoose how much money of deposit we can loose
     * @param maxLeverage set max leverage
     * @param levelPrice fiba prices
     * @return leverage
     */
    public static BigDecimal calculateLeverageLong(
            int percentOfDepositToLoose,
            int maxLeverage,
            Map<FibaLevel, BigDecimal> levelPrice) {
        if (maxLeverage == 1)
            return new BigDecimal(1);

        final MathContext mathContext = new MathContext(3, DOWN);
        final BigDecimal fibaLevel1 = levelPrice.get(ONE);
        final BigDecimal fibaLevel05 = levelPrice.get(FIVE);
        final BigDecimal originalLeverage = new BigDecimal(percentOfDepositToLoose, mathContext)
                .divide(new BigDecimal(100), mathContext)
                .multiply(fibaLevel05.divide(fibaLevel05.subtract(fibaLevel1), mathContext))
                .setScale(0, DOWN);
        final int preparedLeverage = Integer.parseInt(originalLeverage.toString());

        return new BigDecimal(Math.min(preparedLeverage, maxLeverage));
    }

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
            BigDecimal balance,
            BigDecimal levarage
    ) {
        final BigDecimal orderPrice = levelPrice.get(FIVE);
        final BigDecimal quantityWOThreshold = balance
                .multiply(levarage)
                .divide(orderPrice, MathContext.DECIMAL32);
        final BigDecimal quantity = quantityWOThreshold
                .subtract(quantityWOThreshold.multiply(quantityThreshold));
        final int isLeverage = (Objects.equals(levarage, new BigDecimal(1))) ? 0 : 1;
        final String customOrderId = SLTP_PREFIX + generateUUID21();

        return Order.builder()
                .category(OrderCategory.LINEAR)
                .ticker(ticker)
                .orderSide(OrderSide.BUY)
                .type(OrderType.LIMIT)
                .quantity(roundBigDecimalDown(quantity, ROUND_SIGN_QUANTITY))
                .price(roundBigDecimalHalfUp(orderPrice, ROUND_SIGN_PRICE))
                .takeProfit(roundBigDecimalHalfUp(levelPrice.get(THREEEIGHTTWO), ROUND_SIGN_PRICE))
                .stopLoss(roundBigDecimalHalfUp(levelPrice.get(ONE), ROUND_SIGN_PRICE))
                .customOrderId(customOrderId)
                .isLeverage(isLeverage)
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
