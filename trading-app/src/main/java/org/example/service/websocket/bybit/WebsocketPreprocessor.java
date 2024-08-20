package org.example.service.websocket.bybit;

public interface WebsocketPreprocessor {
    void preprocess() throws InterruptedException;

    void setIsColdStartRunning(Boolean isColdStartRunning);
}
