package org.example.processor.fiba;

import org.example.model.FibaCandlesData;
import org.example.model.FibaEnviroment;

import static org.example.utils.FibaHelper.calculateValueForLevel;

public class MoreThanOneHorCandleExists implements UpdateFibaProcessor {

    @Override
    public void process(FibaEnviroment fe, FibaCandlesData fibaCandlesData) {
        if (fe.hourCandlesCount() <= 1)
            return;

        if (fe.incomingCandleHigh().compareTo(fe.fibaHigh()) > 0) {
            fibaCandlesData.updateFibaPrice(calculateValueForLevel(fe.fibaLow(), fe.incomingCandleHigh()));
        } else if (fe.hourCandleLow().compareTo(fibaCandlesData.getLevel05()) <= 0) {
            fibaCandlesData.cleanUp();
        }
    }
}
