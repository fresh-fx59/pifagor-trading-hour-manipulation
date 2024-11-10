package org.example.model;

import org.example.enums.Ticker;

import java.math.BigDecimal;

import static org.example.model.enums.FibaLevel.*;

public class CandleEnvironment {
    private final KlineCandle incomingCandle;
    private final FibaCandlesData fibaCandlesData;
    private final OrdersData ordersData;
    private final KlineCandle hourCandle;
    private final Ticker ticker;
    private final int hourCandlesCount;
    private final BigDecimal fibaLevel05;
    private final BigDecimal fibaLevel0382;
    private final BigDecimal fibaLevel1;
    private final BigDecimal ordersDataFibaLevel0382;
    private final BigDecimal ordersDataFibaLevel1;
    private final BigDecimal ordersDataFibaLevel05;
    private final BigDecimal incomingCandleLow;
    private final BigDecimal incomingCandleHigh;
    private final BigDecimal quantityThreshold;

    // Constructor
    public CandleEnvironment(
            KlineCandle incomingCandle,
            FibaCandlesData fibaCandlesData,
            OrdersData ordersData,
            KlineCandle hourCandle,
            BigDecimal quantityThreshold
    ) {
        this.incomingCandle = incomingCandle;
        this.fibaCandlesData = fibaCandlesData;
        this.ordersData = ordersData;
        this.hourCandle = hourCandle;
        this.ticker = incomingCandle.getTicker();
        this.hourCandlesCount = fibaCandlesData.getCandlesCount();
        this.fibaLevel05 = fibaCandlesData.getLevel05();
        this.fibaLevel0382 = fibaCandlesData.getLevel0382();
        this.fibaLevel1 = fibaCandlesData.getLevel1();
        this.ordersDataFibaLevel0382 = ordersData.fibaLevelsToCompare().get(THREEEIGHTTWO);
        this.ordersDataFibaLevel1 = ordersData.fibaLevelsToCompare().get(ONE);
        this.ordersDataFibaLevel05 = ordersData.fibaLevelsToCompare().get(FIVE);
        this.incomingCandleLow = incomingCandle.getLow();
        this.incomingCandleHigh = incomingCandle.getHigh();
        this.quantityThreshold = quantityThreshold;
    }

    // Getters
    // Record-like Getters
    public KlineCandle incomingCandle() {
        return incomingCandle;
    }

    public FibaCandlesData fibaCandlesData() {
        return fibaCandlesData;
    }

    public OrdersData ordersData() {
        return ordersData;
    }

    public KlineCandle hourCandle() {
        return hourCandle;
    }

    public Ticker ticker() {
        return ticker;
    }

    public BigDecimal fibaLevel05() {
        return fibaLevel05;
    }

    public BigDecimal fibaLevel0382() {
        return fibaLevel0382;
    }

    public BigDecimal fibaLevel1() {
        return fibaLevel1;
    }

    public BigDecimal ordersDataFibaLevel05() {
        return ordersDataFibaLevel05;
    }

    public BigDecimal quantityThreshold() {
        return quantityThreshold;
    }

    // Business Logic Methods
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
        final BigDecimal lostAmount = buyAmount.subtract(stopLossAmount);
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
        return hourCandlesCount >= 2;
    }

    public boolean isOrderData05Exists() {
        return ordersDataFibaLevel05 != null;
    }

    public boolean isFiba05GreaterOrdersData05() {
        return fibaLevel05.compareTo(ordersDataFibaLevel05) > 0;
    }
}
