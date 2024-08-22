package org.example.processor.fiba;

import lombok.extern.slf4j.Slf4j;
import org.example.model.FibaCandlesData;
import org.example.model.FibaEnviroment;
import org.example.model.enums.FibaProcessorState;

import static org.example.utils.FibaHelper.calculateValueForLevel;

@Slf4j
public class OneHourCandleProcessor implements UpdateFibaProcessor {
    @Override
    public FibaProcessorState process(FibaEnviroment fe, FibaCandlesData fibaCandlesData) {
        if (fe.hourCandleHigh().compareTo(fe.fibaHigh()) <= 0 // hour candle didn't update the highest point
                || fe.hourCandleLow().compareTo(fe.fibaFive()) <= 0) // hour candle drop below 0.5 fiba
            return FibaProcessorState.CLEAN_UP_FIBA_DATA;

        // update the highest point for fiba from hour candle or set hour candle low and high for cleaned up fiba
        fibaCandlesData.updateFibaPrice(calculateValueForLevel(fe.fibaLow(), fe.hourCandleHigh()));
        fibaCandlesData.addCandle(fe.hourCandle());
        log.info("""
                        fiba processor ONE HOUR CANDLE:
                        hour candles {}, hour of incoming candle {}
                        fiba data
                        {}
                        incoming candle
                        {}
                        """,
                fibaCandlesData.getCandlesCount() - 1,
                fe.incomingCandle().getOpenAt().getHour(),
                fibaCandlesData.fibaPriceLevels(),
                fe.incomingCandle());

        return FibaProcessorState.MORE_THAN_ONE_HOUR_CANDLE;
    }
}
