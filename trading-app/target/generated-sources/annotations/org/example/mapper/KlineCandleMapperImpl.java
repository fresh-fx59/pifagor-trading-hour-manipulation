package org.example.mapper;

import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import javax.annotation.processing.Generated;
import org.example.enums.Ticker;
import org.example.enums.TickerInterval;
import org.example.model.UniversalKlineCandle;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-07-07T22:07:21+0300",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.11 (Eclipse Adoptium)"
)
public class KlineCandleMapperImpl extends KlineCandleMapper {

    @Override
    public UniversalKlineCandle toUniversalKlineCandle(KlineData klineData) {
        if ( klineData == null ) {
            return null;
        }

        LocalDateTime closeAt = LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(klineData.getEnd()), java.time.ZoneOffset.UTC);
        TickerInterval tickerInterval = org.example.enums.TickerInterval.getTickerIntervalFromBybitValue(klineData.getInterval());
        LocalDateTime eventTime = LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(klineData.getTimestamp()), java.time.ZoneOffset.UTC);
        Boolean isKlineClosed = null;
        Ticker ticker = null;
        ZoneOffset s = null;

        UniversalKlineCandle universalKlineCandle = new UniversalKlineCandle( isKlineClosed, closeAt, tickerInterval, ticker, eventTime, s );

        if ( klineData.getOpen() != null ) {
            universalKlineCandle.setOpen( new BigDecimal( klineData.getOpen() ) );
        }
        if ( klineData.getHigh() != null ) {
            universalKlineCandle.setHigh( new BigDecimal( klineData.getHigh() ) );
        }
        if ( klineData.getLow() != null ) {
            universalKlineCandle.setLow( new BigDecimal( klineData.getLow() ) );
        }
        if ( klineData.getClose() != null ) {
            universalKlineCandle.setClose( new BigDecimal( klineData.getClose() ) );
        }

        universalKlineCandle.setOpenAt( LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(klineData.getStart()), java.time.ZoneOffset.UTC) );

        return universalKlineCandle;
    }
}
