package org.example.processor.candle;

import org.example.model.FibaCandlesData;
import org.example.model.KlineCandle;
import org.example.model.OrdersData;

import java.math.BigDecimal;

/**
 * Orders data is not FREEZED
 *  Check if there is less than 2 candles
 *    YES - exit
 *    NO - proceed to next checks
 *  Check if there is 2 hour candles and it is last minute of hour
 *    YES - update fiba levels and create order
 *    NO - go to next check
 *  Check if fibaLevel05 differ from order data level 05
 *    YES amend orders
 *    NO proceed to next check
 *  Check if candle low lower than fiba level 05
 *    YES set FREEZED to true and exit
 *    NO proceed to next check
 *
 * Orders data is FREEZED
 *  Check if candle high is higher than fibaLevel0382
 *    YES check order status and if filled
 *      - cancel opened orders
 *    NO process next checks
 *  Check if orders data is freezed and candle low is below fiba ONE
 *    YES check order status if ONE order was filled, then
 *      cancel opened orders
 *      set FREEZED to FALSE
 *    NO process next checks
 *
 */
public interface CandleProcessor {
    void process(
            KlineCandle incomingCandle,
            KlineCandle hourCandle,
            FibaCandlesData fibaCandlesData,
            OrdersData ordersData,
            BigDecimal quantityThreshold
    );
}
