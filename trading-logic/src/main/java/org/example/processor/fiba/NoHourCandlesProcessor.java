package org.example.processor.fiba;

import org.example.model.FibaCandlesData;
import org.example.model.FibaEnviroment;
import org.example.model.enums.FibaLevel;
import org.example.model.enums.FibaProcessorState;

import java.math.BigDecimal;
import java.util.Map;

import static org.example.utils.FibaHelper.calculateValueForLevel;

public class NoHourCandlesProcessor implements UpdateFibaProcessor {

    @Override
    public FibaProcessorState process(FibaEnviroment fe, FibaCandlesData fibaCandlesData) {
        fibaCandlesData.addCandle(fe.hourCandle());
        Map<FibaLevel, BigDecimal> fibaLevelsValues = calculateValueForLevel(fe.hourCandleLow(), fe.hourCandleHigh());
        fibaCandlesData.updateFibaPrice(fibaLevelsValues);

        return FibaProcessorState.ONE_HOUR_CANDLE;
    }
}
