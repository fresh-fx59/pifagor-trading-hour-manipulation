package org.example.processor.fiba;

import org.example.model.FibaCandlesData;
import org.example.model.FibaEnviroment;

public interface UpdateFibaProcessor {
    void process(FibaEnviroment fibaEnviroment, FibaCandlesData fibaCandlesData);
}
