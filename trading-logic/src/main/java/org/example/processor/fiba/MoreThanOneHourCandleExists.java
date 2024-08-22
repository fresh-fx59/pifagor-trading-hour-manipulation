package org.example.processor.fiba;

import lombok.extern.slf4j.Slf4j;
import org.example.model.FibaCandlesData;
import org.example.model.FibaEnviroment;

import static org.example.utils.FibaHelper.calculateValueForLevel;

/**
 * Case 3 there is more than 1 hour candles in fiba
 *   1. check that there is more than one candle in fiba
 *   2. check that candle's high is more than fiba high
 *     a. YES update fiba levels
 *     b. NO check if its low is lower than 0.5 fiba
 *        - NO do nothing
 *        - YES clean up fiba
 */
@Slf4j
public class MoreThanOneHourCandleExists implements UpdateFibaProcessor {

    @Override
    public void process(FibaEnviroment fe, FibaCandlesData fibaCandlesData) {
        if (fe.hourCandlesCount() <= 1)
            return;

        if (fe.isClosingHourCandle())
            fibaCandlesData.addCandle(fe.hourCandle());

        if (fe.incomingCandleHigh().compareTo(fe.fibaHigh()) > 0) {
            log.info("""
                        fiba processor MORE THAN ONE HOUR CANDLE: fibaCandlesData update fiba price
                        hour candles {}, hour of incoming candle {},
                        fiba data
                        {}
                        current candle
                        {}
                        """,
                    fe.hourCandlesCount(),
                    fe.incomingCandle().getOpenAt().getHour(),
                    fibaCandlesData.fibaPriceLevels(),
                    fe.incomingCandle());
            fibaCandlesData.updateFibaPrice(calculateValueForLevel(fe.fibaLow(), fe.incomingCandleHigh()));
        } else if (fe.hourCandleLow().compareTo(fibaCandlesData.getLevel05()) <= 0) {
            log.info("""
                        fiba processor MORE THAN ONE HOUR CANDLE: fibaCandlesData clean up
                        hour candles {}, hour of incoming candle {},
                        fiba data
                        {}
                        incoming candle
                        {}
                        """,
                    fe.hourCandlesCount(),
                    fe.incomingCandle().getOpenAt().getHour(),
                    fibaCandlesData.fibaPriceLevels(),
                    fe.incomingCandle());
            fibaCandlesData.cleanUp();
        }
    }
}
