package org.example.processor.fiba.state.impl;

import org.example.model.FibaCandlesData;
import org.example.model.FibaEnviroment;
import org.example.model.enums.FibaProcessorState;
import org.example.processor.fiba.state.FibaState;

public class CleanUpFibaCandlesDataState implements FibaState {
    @Override
    public FibaProcessorState getNext(FibaEnviroment fe, FibaCandlesData fibaCandlesData) {
        fibaCandlesData.cleanUp();
        return FibaProcessorState.NO_HOUR_CANDLES;
    }
}
