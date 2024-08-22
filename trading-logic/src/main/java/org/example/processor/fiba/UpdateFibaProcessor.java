package org.example.processor.fiba;

import org.example.model.FibaCandlesData;
import org.example.model.FibaEnviroment;
import org.example.model.enums.FibaProcessorState;

public interface UpdateFibaProcessor {
    FibaProcessorState process(FibaEnviroment fibaEnviroment, FibaCandlesData fibaCandlesData);
}
