package org.example.processor.fiba;

import lombok.extern.slf4j.Slf4j;
import org.example.model.FibaCandlesData;
import org.example.model.FibaEnviroment;

import java.math.BigDecimal;

import static org.example.utils.FibaHelper.calculateValueForLevel;

@Slf4j
public class OneHourCandleProcessor implements UpdateFibaProcessor {

    @Override
    public void process(FibaEnviroment fe, FibaCandlesData fibaCandlesData) {
        if (!fe.isHourCandleOpened()
                || fe.hourCandlesCount() != 1
                || !fe.isClosingHourCandle()
                || !fe.isCandleClosed())
            return;

        BigDecimal low;

        if (fe.hourCandleHigh().compareTo(fe.fibaHigh()) <= 0 // hour candle didn't update the highest point
                || fe.hourCandleLow().compareTo(fe.fibaFive()) <= 0) { // hour candle drop below 0.5 fiba
            fibaCandlesData.cleanUp();
            low = fe.hourCandleLow();
        } else {
            low = fe.fibaLow();
        }

        // update the highest point for fiba from hour candle or set hour candle low and high for cleaned up fiba
        fibaCandlesData.updateFibaPrice(calculateValueForLevel(low, fe.hourCandleHigh()));
        fibaCandlesData.addCandle(fe.hourCandle());

        log.info("""
                        fiba processor ONE HOUR CANDLE:
                        hour candles {}, hour of incoming candle {}
                        fiba data
                        {}
                        """,
                fibaCandlesData.getCandlesCount(),
                fe.incomingCandle().getOpenAt().getHour(),
                fibaCandlesData.fibaPriceLevels());
    }
}
