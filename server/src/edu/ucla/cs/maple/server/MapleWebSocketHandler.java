package edu.ucla.cs.maple.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.ucla.cs.maple.check.UseChecker;
import edu.ucla.cs.model.APICall;
import edu.ucla.cs.model.APISeqItem;
import edu.ucla.cs.model.Pattern;
import edu.ucla.cs.model.Violation;
import edu.ucla.cs.utils.PatternUtils;

@WebSocket
public class MapleWebSocketHandler {
	Session session = null;
	boolean _isFirstMessage = true;

	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		this.session = null;
		System.out.println("Close: statusCode=" + statusCode + ", reason="
				+ reason);
	}

	@OnWebSocketError
	public void onError(Throwable t) {
		System.out.println("Error: " + t.getMessage());
	}

	@OnWebSocketConnect
	public void onConnect(Session session) {
		this.session = session;
		System.out.println("Connect: "
				+ session.getRemoteAddress().getAddress());
	}

	@OnWebSocketMessage
	public void onMessage(String message) {
		/* TEST */
		//System.out.println("Message: " + message);
	    
	    ObjectMapper mapper = new ObjectMapper();
	    HashMap<Pattern, ArrayList<Violation>> vioMap = new HashMap<Pattern, ArrayList<Violation>>();

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

			vioMap = parseAndAnalyzeCodeSnippet(snippets);
			
			// TODO: send patterns + ids back to the plugin, along with the
            // analyzed snippet so the plugin knows where to highlight
			for (Pattern p : vioMap.keySet()) {
			    for (Violation v : vioMap.get(p)) {
			        /* TEST */
			        //System.out.println(v.getViolationMessage(p));
			    }
			}
		} else {
			// any following messages will be up/downvotes. We know which
			// pattern
			// is being voted on based on the vote's id, whose index corresponds
			// with the page of its pattern
			JsonNode voteMessage;
			try {
				voteMessage = mapper.readValue(message, JsonNode.class);

				/* TEST */
				System.out.println("Vote:" + voteMessage.get("vote").asText()
						+ ", ID:" + voteMessage.get("id").asText());

				// TODO: send to MySQL
				// TODO: how to find corresponding pattern? database should send
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
	
	private HashMap<Pattern, ArrayList<Violation>> parseAndAnalyzeCodeSnippet(CodeSnippet[] snippets) {
        HashMap<String, ArrayList<Pattern>> patterns = new HashMap<String, ArrayList<Pattern>>();
	    HashMap<Pattern, ArrayList<Violation>> vioMap = new HashMap<Pattern, ArrayList<Violation>>();
	    MySQLAccess dbAccess = new MySQLAccess();
	    
	    // only connect it once
        dbAccess.connect();

        // analyze the code snippets and return patterns to the plugin
        for (CodeSnippet cs : snippets) {
            try {
                // parse and analyze a snippet using Maple
                PartialProgramAnalyzer analyzer = new PartialProgramAnalyzer(
                        cs.getSnippet());
                ArrayList<Pattern> ps; 
                HashMap<String, ArrayList<APISeqItem>> seqs = analyzer
                        .retrieveAPICallSequences();
                for (String method : seqs.keySet()) {
                    // get the corresponding call sequence for a method
                    // in the snippet
                    ArrayList<APISeqItem> seq = seqs.get(method);
                    
                    //TEST
                    System.out.println("Method" + method);

                    for (APISeqItem item : seq) {
                        if (!(item instanceof APICall)) {
                            continue;
                        }

                        //String name = ((APICall) item).name;
                        String name = ((APICall) item).getName();
                        
                        //TEST
                        System.out.println("APICall name: " + name);
                        
                        if (patterns.containsKey(name)) {
                            // the pattern of this API method exists
                            ps = patterns.get(name); 
                        } else {
                            // get patterns from the database
                            ps = dbAccess.getPatterns(name);
                            patterns.put(name, ps);
                        }
                        
                        // check for violation or alternative usage
                        for (Pattern p : ps) {
                            // parse the String pattern into an
                            // ArrayList<APISeqItem>
                            HashSet<ArrayList<APISeqItem>> pset = new HashSet<ArrayList<APISeqItem>>();
                            
                            ArrayList<APISeqItem> pArray = PatternUtils
                                    .convert(p.pattern);
                            pset.add(pArray);
                            
                            if(!p.alternative.isEmpty()) {
                                HashSet<ArrayList<APISeqItem>> pset2 = PatternUtils.getAlternativePatterns(p);
                                pset.addAll(pset2);
                            }
                            
                            // check for API misuse
                            UseChecker checker = new UseChecker();
                            // get violations and violating patterns
                            ArrayList<Violation> vios = checker.validate(pset, seq);
                            Pattern vioPattern = PatternUtils.getThisOrAlternativePattern(p, checker.pattern);
                            vioMap.put(vioPattern, vios);
                        }
                    }
                }
            } catch (Exception e) {
                // do nothing if we get an exception when parsing and
                // analyzing the snippet
                continue;
            }
        }
        dbAccess.close();
        return vioMap;
	}
}
