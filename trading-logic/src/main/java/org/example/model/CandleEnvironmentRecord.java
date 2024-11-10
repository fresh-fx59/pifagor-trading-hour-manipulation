package org.example.model;

import org.example.enums.Ticker;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.example.model.enums.FibaLevel.*;
import static org.example.model.enums.OrdersDataParams.ORDERS_CREATED;

public record CandleEnvironmentRecord(
        KlineCandle incomingCandle,
        FibaCandlesData fibaCandlesData,
        OrdersData ordersData,
        KlineCandle hourCandle,
        Ticker ticker,
        int hourCandlesCount,
        LocalDateTime candlesTime,
        BigDecimal fibaLevel05,
        BigDecimal fibaLevel0382,
        BigDecimal fibaLevel1,
        BigDecimal ordersDataFibaLevel0382,
        BigDecimal ordersDataFibaLevel1,
        BigDecimal ordersDataFibaLevel05,
        boolean isNotFreezed, //to be deleted
        boolean isFreezed, // to be deleted
        boolean ordersNotCreated, // to be deleted
        BigDecimal incomingCandleLow,
        BigDecimal incomingCandleHigh,
        BigDecimal quantityThreshold

) {
    public CandleEnvironmentRecord(
            KlineCandle incomingCandle,
            FibaCandlesData fibaCandlesData,
            OrdersData ordersData,
            KlineCandle hourCandle,
            BigDecimal quantityThreshold
    ) {
        this(
                incomingCandle,
                fibaCandlesData,
                ordersData,
                hourCandle,
                incomingCandle.getTicker(),
                fibaCandlesData.getCandlesCount(),
                incomingCandle.getOpenAt(),
                fibaCandlesData.getLevel05(),
                fibaCandlesData.getLevel0382(),
                fibaCandlesData.getLevel1(),
                ordersData.fibaLevelsToCompare().get(THREEEIGHTTWO),
                ordersData.fibaLevelsToCompare().get(ONE),
                ordersData.fibaLevelsToCompare().get(FIVE),
                ordersData.isNotFreezed(),
                ordersData.isFreezed(),
                !ordersData.getParam(ORDERS_CREATED, false),
                incomingCandle.getLow(),
                incomingCandle.getHigh(),
                quantityThreshold
        );
    }

    private BigDecimal getOrderTakeProfit() {
        if (ordersData.order() != null)
            return new BigDecimal(ordersData.order().getTakeProfit());
        throw new IllegalStateException("Order take profit should be filled, however order is null");
    }

    private BigDecimal getOrderStopLoss() {
        if (ordersData.order() != null)
            return new BigDecimal(ordersData.order().getStopLoss());
        throw new IllegalStateException("Order stopLoss should be filled, however order is null");
    }

    private BigDecimal getOrderQuantity() {
        if (ordersData.order() != null)
            return new BigDecimal(ordersData.order().getQuantity());
        throw new IllegalStateException("Order quantity should be filled, however order is null");
    }

    private BigDecimal getOrderPrice() {
        if (ordersData.order() != null)
            return new BigDecimal(ordersData.order().getPrice());
        throw new IllegalStateException("Order price should be filled, however order is null");
    }

    public boolean isCandleLowLowerOrEqualOrder1() {
        return incomingCandle.getLow().compareTo(ordersDataFibaLevel1) <= 0;
    }

    public String getCustomOrderId() {
        return ordersData.order().getCustomOrderId();
    }


    public BigDecimal getBuyMinusLostAmount() {
        final BigDecimal stopLossAmount = getOrderStopLoss().multiply(getOrderQuantity());
        final BigDecimal buyAmount = getOrderPrice().multiply(getOrderQuantity());
        BigDecimal lostAmount = buyAmount.subtract(stopLossAmount);
        return buyAmount.subtract(lostAmount);
    }

    public BigDecimal getProfitAmount() {
        return getOrderTakeProfit().multiply(getOrderQuantity());
    }

    public boolean isIncomingCandleHighHigherOrEqualOrderData0382() {
        return incomingCandleHigh.compareTo(ordersDataFibaLevel0382) >= 0;
    }

    public boolean isIncomingCanldeLowLowerOrEqualOrderData05() {
        return incomingCandleLow.compareTo(ordersDataFibaLevel05) <= 0;
    }

    public boolean isMoreThan2HourCandles() {
        return hourCandlesCount() >= 2;
    }

    public boolean isOrderData05Exists() {
        return ordersDataFibaLevel05 != null;
    }

    public boolean isFiba05GreaterOrdersData05() {
        return fibaLevel05.compareTo(ordersDataFibaLevel05) > 0;
    }
}
