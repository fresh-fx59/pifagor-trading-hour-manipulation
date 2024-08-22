package org.example.processor.fiba;

import org.example.model.FibaCandlesData;
import org.example.model.FibaEnviroment;
import org.example.model.enums.FibaProcessorState;

public class CleanUpFibaCandlesData implements UpdateFibaProcessor {
    @Override
    public FibaProcessorState process(FibaEnviroment fe, FibaCandlesData fibaCandlesData) {
        fibaCandlesData.cleanUp();
        return FibaProcessorState.NO_HOUR_CANDLES;
    }
}
