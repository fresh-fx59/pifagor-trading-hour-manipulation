package org.example.model;

import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import lombok.Getter;
import org.example.enums.LoadType;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;


@Getter
public class BybitKlineDataForStatement extends KlineData {
    private final Ticker ticker;
    private final TickerInterval tickerInterval;
    private final LoadType loadType;

    public BybitKlineDataForStatement(KlineData klineData, Ticker ticker, TickerInterval tickerInterval, LoadType loadType) {
        this.ticker = ticker;
        this.tickerInterval = tickerInterval;
        this.loadType = loadType;
        this.setStart(klineData.getStart());
        this.setEnd(klineData.getEnd());
        this.setInterval(klineData.getInterval());
        this.setOpen(klineData.getOpen());
        this.setClose(klineData.getClose());
        this.setHigh(klineData.getHigh());
        this.setLow(klineData.getLow());
        this.setVolume(klineData.getVolume());
        this.setTurnover(klineData.getTurnover());
        this.setConfirm(klineData.getConfirm());
        this.setTimestamp(klineData.getTimestamp());
    }
}
