import endpoint.SendKlineWebsocket;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.tyrus.server.Server;

/**
 * For wss use
 * keytool -genkeypair -alias myserver -keyalg RSA -keystore keystore.jks -keysize 2048
 */
@Slf4j
public class WebSocketServer {

    public static void main(String[] args) {
        final String host = "localhost";
        final int port = 8067;
        final String contextPath = "/websocket";

        Server server = new Server(host, port, contextPath, null, SendKlineWebsocket.class);

        try {
            server.start();
            log.info("WebSocket server started at ws://{}:{}{}/*",
                    host, port, contextPath);
            // Keep the server running
            Thread.currentThread().join();
        } catch (Exception e) {
            log.error("failed to run websocket", e);
        } finally {
            server.stop();
        }
    }
}
