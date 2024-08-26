package org.example.processor.fiba;

import org.example.model.FibaCandlesData;
import org.example.model.FibaEnviroment;
import org.example.model.enums.FibaProcessorState;

import static org.example.model.enums.FibaProcessorState.CLEAN_UP_FIBA_DATA;
import static org.example.model.enums.FibaProcessorState.MORE_THAN_ONE_HOUR_CANDLE;
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
public class MoreThanOneHourCandleExists implements UpdateFibaProcessor {

    @Override
    public FibaProcessorState process(FibaEnviroment fe, FibaCandlesData fibaCandlesData) {
        if (fe.isClosingHourCandle())
            fibaCandlesData.addCandle(fe.hourCandle());

        if (fe.incomingCandleHigh().compareTo(fe.fibaHigh()) > 0) {
            fibaCandlesData.updateFibaPrice(calculateValueForLevel(fe.fibaLow(), fe.incomingCandleHigh()));
        } else if (fe.hourCandleLow().compareTo(fibaCandlesData.getLevel05()) <= 0) {
            return CLEAN_UP_FIBA_DATA;
        }

        return MORE_THAN_ONE_HOUR_CANDLE;
    }
}
