package org.example.processor.fiba.state;

import org.example.model.FibaCandlesData;
import org.example.model.FibaEnviroment;
import org.example.model.enums.FibaProcessorState;

public interface FibaState {
    FibaProcessorState getNext(FibaEnviroment fibaEnviroment, FibaCandlesData fibaCandlesData);
}
