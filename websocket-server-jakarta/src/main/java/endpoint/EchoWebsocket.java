package endpoint;

import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/echo")
public class EchoWebsocket {
    @OnMessage
    public String onMessage(String message, Session session) {
        System.out.println("Received message: " + message);
        return "Echo: " + message; // Echo the received message back to the client
    }
}
