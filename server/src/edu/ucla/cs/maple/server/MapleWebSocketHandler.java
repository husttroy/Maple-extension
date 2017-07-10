package edu.ucla.cs.maple.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ucla.cs.model.APICall;
import edu.ucla.cs.model.APISeqItem;
import edu.ucla.cs.model.ControlConstruct;
import edu.ucla.cs.model.MethodCall;


@WebSocket
public class MapleWebSocketHandler {
	Session session = null;
	boolean _isFirstMessage = true;
	
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
        /* TEST */
        System.out.println("Message: " + message);
        
        ObjectMapper mapper = new ObjectMapper();
        MySQLAccess dbAccess = new MySQLAccess();
        
        PartialProgramAnalyzer analyzer = null;
        HashMap<APISeqItem, HashMap<Integer, ArrayList<APISeqItem>>> patterns = 
                new HashMap<APISeqItem, HashMap<Integer, ArrayList<APISeqItem>>>();
        HashMap<Integer, ArrayList<APISeqItem>> newPatterns = 
                new HashMap<Integer, ArrayList<APISeqItem>>();
        
        // the first message will always be the code from the HTML page
        if (_isFirstMessage) {
            _isFirstMessage = false;
            
            // parse the JSON objects out of the message
            CodeSnippet[] snippets = null;
            try {
                // Convert JSON string to array of CodeSnippets
                snippets = mapper.readValue(message, CodeSnippet[].class);
                
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace(); 
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // analyze the code snippets and return patterns to the plugin
            for (CodeSnippet cs : snippets) {
                try {
                    // parse and analyze the code snippets using Maple
                    analyzer = new PartialProgramAnalyzer(cs.getSnippet());
                    
                    HashMap<String, ArrayList<APISeqItem>> callSeq = analyzer.retrieveAPICallSequences();
                    dbAccess.connect();
                    
                    for (String key : callSeq.keySet()) {
                        for (APISeqItem item : callSeq.get(key)) {
                            // get matching patterns from the database and parse
                            // into ArrayList<APISeqItem>
                            HashMap<Integer, String> dbPatterns = dbAccess.getPatterns(((APICall) item).getName());
                            
                            for (Integer patternKey : dbPatterns.keySet()) {
                                // parse the String pattern into an ArrayList<APISeqItem>
                                String[] strSeqItems = dbPatterns.get(patternKey).split(";");
                                ArrayList<APISeqItem> currentPattern = new ArrayList<APISeqItem>();
                                
                                for (String strItem : strSeqItems) {
                                    // instantiate either a ControlConstruct or
                                    // an APICallItem and add to currentPattern
                                    switch (strItem) {
                                        case "try {":
                                            currentPattern.add(ControlConstruct.TRY);
                                            break;
                                        case "catch {":
                                            currentPattern.add(ControlConstruct.CATCH);
                                            break;
                                        case "finally {":
                                            currentPattern.add(ControlConstruct.FINALLY);
                                            break;
                                        case "if {":
                                            currentPattern.add(ControlConstruct.IF);
                                            break;
                                        case "else {":
                                            currentPattern.add(ControlConstruct.ELSE);
                                            break;
                                        // TODO case LOOP and END_BLOCK
                                        // default is APICallItem
                                        default: 
                                            //APICall(name, condition, args);                                            
                                    }
                                }
                                // put the new pattern and its id in newPatterns
                                //newPatterns.put(patternKey, value);
                            }
                          // add newPatterns to patterns using the corresponding
                          // APISeqItem extracted from SO as its key
                          //patterns.put(item, newPatterns);
                        }
                    }
                    
                    dbAccess.close();
                    
                    // TODO compare patterns with original snippet to look for
                    // API usage violations
                    
                    // TODO send patterns + ids back to the plugin, along with the
                    // analyzed snippet so the plugin knows where to highlight
                } catch (Exception e) {
                    // if we get an exception, the code was not able to be
                    // parsed/analyzed. So we do nothing and continue analysis.
                }
            }
        }
        else {
            // any following messages will be up/downvotes. We know which pattern
            // is being voted on based on the vote's id, whose index corresponds
            // with the page of its pattern
            
            JsonNode voteMessage;
            try {
                voteMessage = mapper.readValue(message, JsonNode.class);
                
                /* TEST */
                System.out.println("Vote:" + voteMessage.get("vote").asText() + ", ID:"
                        + voteMessage.get("id").asText());
                
                //TODO: send to MySQL
                //TODO: how to find corresponding pattern? database should send
                // pattern + id when populating plugin
                
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace(); 
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
