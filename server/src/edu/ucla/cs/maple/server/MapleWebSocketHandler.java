package edu.ucla.cs.maple.server;

import java.io.IOException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@WebSocket
public class MapleWebSocketHandler {
	Session session = null;
	
	@OnWebSocketClose
    public void onClose(int statusCode, String reason) {
		this.session = null;
        System.out.println("Close: statusCode=" + statusCode + ", reason=" + reason);
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        System.out.println("Error: " + t.getMessage());
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
    	this.session = session;
        System.out.println("Connect: " + session.getRemoteAddress().getAddress());
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        ObjectMapper mapper = new ObjectMapper();
        
        //TEST
        //System.out.println("Message: " + message);
        
        // parse the JSON objects out of the message
        CodeSnippet[] snippets;
        try {
            // Convert JSON string to array of CodeSnippets
            snippets = mapper.readValue(message, CodeSnippet[].class);
            
            //TEST
            //for (CodeSnippet snip : snippets) {
                //System.out.println(snip);
            //}
            
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
