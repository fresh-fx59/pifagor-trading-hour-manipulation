package org.example.processor.fiba;

import lombok.extern.slf4j.Slf4j;
import org.example.model.FibaCandlesData;
import org.example.model.FibaEnviroment;
import org.example.model.enums.FibaLevel;
import org.example.model.enums.FibaProcessorState;

import java.math.BigDecimal;
import java.util.Map;

import static org.example.utils.FibaHelper.calculateValueForLevel;

@Slf4j
public class NoHourCandlesProcessor implements UpdateFibaProcessor {

    @Override
    public FibaProcessorState process(FibaEnviroment fe, FibaCandlesData fibaCandlesData) {
        fibaCandlesData.addCandle(fe.hourCandle());
        Map<FibaLevel, BigDecimal> fibaLevelsValues = calculateValueForLevel(fe.hourCandleLow(), fe.hourCandleHigh());
        fibaCandlesData.updateFibaPrice(fibaLevelsValues);

        log.info("""
                        fiba processor NO HOUR CANDLE:
                        hour candles {}, hour of incoming candle {},
                        fiba data
                        {}
                        incoming candle
                        {}
                        """,
                fibaCandlesData.getCandlesCount() - 1,
                fe.incomingCandle().getOpenAt().getHour(),
                fibaCandlesData.fibaPriceLevels(),
                fe.incomingCandle());

        return FibaProcessorState.ONE_HOUR_CANDLE;
    }
}
