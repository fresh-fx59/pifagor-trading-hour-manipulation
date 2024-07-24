package org.example.processor.fiba;

import lombok.extern.slf4j.Slf4j;
import org.example.model.FibaCandlesData;
import org.example.model.FibaEnviroment;
import org.example.model.enums.FibaLevel;

import java.math.BigDecimal;
import java.util.Map;

import static org.example.utils.FibaHelper.calculateValueForLevel;

@Slf4j
public class NoHourCandlesProcessor implements UpdateFibaProcessor {

    @Override
    public void process(FibaEnviroment fe, FibaCandlesData fibaCandlesData) {
        if (!fe.isHourCandleOpened() || !fe.isHourCandlesEmpty() || !fe.isClosingHourCandle())
            return;

        fibaCandlesData.addCandle(fe.hourCandle());
        Map<FibaLevel, BigDecimal> fibaLevelsValues = calculateValueForLevel(fe.hourCandleLow(), fe.hourCandleHigh());
        fibaCandlesData.updateFibaPrice(fibaLevelsValues);

        log.info("""
                        fiba processor NO HOUR CANDLE:
                        hour candles {}, hour of incoming candle {},
                        fiba data
                        {}
                        """,
                fe.hourCandlesCount(),
                fe.incomingCandle().getOpenAt().getHour(),
                fibaCandlesData.fibaPriceLevels());
    }
}
