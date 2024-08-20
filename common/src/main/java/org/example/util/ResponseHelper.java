package org.example.util;

import org.example.enums.Ticker;
import org.example.enums.TickerInterval;

public class ResponseHelper {
    public static String getBybitTopic(Ticker ticker, TickerInterval interval) {
        return "kline." + interval.getBybitValue() + "." + ticker.getBybitValue();
    }
}
