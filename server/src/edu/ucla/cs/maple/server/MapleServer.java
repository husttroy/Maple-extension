package edu.ucla.cs.maple.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class MapleServer {
	public static void main(String[] args) throws Exception {
        Server server = new Server(4000);
        WebSocketHandler wsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(MapleWebSocketHandler.class);
            }
        };
        server.setHandler(wsHandler);
        server.start();
        server.join();
    }
}
