package org.example.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

public class WebsocketService {
    private static final String WEBSOCKET_URI = "ws://localhost:8080/websocket";

    public void sender() {
//        try {
//            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
//            Session session = container.connectToServer(WebSocketSender.class, new URI(WEBSOCKET_URI));
//
//            // Send text message
//            session.getBasicRemote().sendText("Hello, WebSocket!");
//
//            // Send binary message
//            byte[] binaryData = {0x00, 0x01, 0x02, 0x03};
//            session.getBasicRemote().sendBinary(ByteBuffer.wrap(binaryData));
//
//            // Close the session
//            session.close();
//        } catch (DeploymentException | IOException |
//                 URISyntaxException e) {
//            e.printStackTrace();
//        }
    }

}
