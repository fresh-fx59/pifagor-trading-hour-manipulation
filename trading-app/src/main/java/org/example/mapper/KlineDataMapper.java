package org.example.mapper;

import com.bybit.api.client.domain.websocket_message.public_channel.KlineData;
import org.example.model.UniversalKlineCandle;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class KlineDataMapper {

    @Mapping(target = "openAt", expression = "java(LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(klineData.getStart()), java.time.ZoneOffset.UTC))")
    @Mapping(target = "closeAt", expression = "java(LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(klineData.getEnd()), java.time.ZoneOffset.UTC))")
    @Mapping(target = "tickerInterval", expression = "java(org.example.enums.TickerInterval." +
            "getTickerIntervalFromBybitValue(klineData.getInterval()))")
    @Mapping(target = "eventTime", expression = "java(LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(klineData.getTimestamp()), java.time.ZoneOffset.UTC))")
    @InheritConfiguration
    public abstract UniversalKlineCandle toUniversalKlineCandle(KlineData klineData);
}
