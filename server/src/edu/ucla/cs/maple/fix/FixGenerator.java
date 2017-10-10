package edu.ucla.cs.maple.fix;

import java.util.ArrayList;

import edu.ucla.cs.model.APICall;
import edu.ucla.cs.model.APISeqItem;
import edu.ucla.cs.model.ControlConstruct;

public class FixGenerator {
	
	public String generate(ArrayList<APISeqItem> pattern, ArrayList<APISeqItem> mSeq) {
		String fix = "";
		for(int i = 0; i < pattern.size(); i++) {
			APISeqItem item = pattern.get(i);
			if(item instanceof ControlConstruct) {
				if(item.equals(ControlConstruct.IF)) {
					// look forward to find an API call with guard condition
					String guard = getContextualizedGuardCondition(i, pattern, mSeq);
					if(guard != null) {
						fix += "if (" + guard + ") {";
					}
				} else if (item.equals(ControlConstruct.ELSE)) {
					fix += "else {";
				} else if (item.equals(ControlConstruct.TRY)) {
					fix += "try {";
				} else if (item.equals(ControlConstruct.CATCH)) {
					fix += "catch (Exception e)";
				} else if (item.equals(ControlConstruct.LOOP)) {
					// look forward to find an API call with guard condition
					String guard = getContextualizedGuardCondition(i, pattern, mSeq);
					if(guard != null) {
						fix += "while (" + guard + ") {";
					}
				} else if (item.equals(ControlConstruct.FINALLY)) {
					fix += "finally {";
				} else if (item.equals(ControlConstruct.END_BLOCK)) {
					fix += "}";
				}
			} else {
				APICall call = (APICall)item;
				APICall counterpart = findCounterpartCall(call, mSeq);
				if(counterpart != null) {
					if(counterpart.receiver != null) {
						fix += counterpart.receiver + ".";
					}
					fix += counterpart.name.substring(0, counterpart.name.indexOf("(") + 1);
					for(int j = 0; j < counterpart.arguments.size(); j++) {
						if(j == counterpart.arguments.size() - 1) {
							fix +=  counterpart.arguments.get(j);
						} else {
							fix += counterpart.arguments.get(j) + ",";
						}
					}
					fix += ");";
				} else {
					if(call.receiver_type != null) {
						if(call.receiver_type.equals("unresolved")) {
							fix += "var." + call.name.substring(0, call.name.indexOf("(") + 1);
						} else {
							fix += call.receiver_type.toLowerCase() + "." + call.name.substring(0, call.name.indexOf("(") + 1); 
						}
					} else {
						// constructor
						fix += call.name.substring(0, call.name.indexOf("(") + 1);
					}
					
					
					String argNumS = call.name.substring(call.name.indexOf("(") + 1, call.name.indexOf(")"));
					int argNum = Integer.parseInt(argNumS); 
					for(int j = 0; j < argNum; j++) {
						if(j == argNum - 1) {
							fix += "arg" + j;
						} else {
							fix += "arg" + j + ",";
						}
					}
					fix += ");";
				}
			}
			
			fix += System.lineSeparator();
		}
		
		return fix;
	}
	
	private String getContextualizedGuardCondition(int startIndex, ArrayList<APISeqItem> pattern, ArrayList<APISeqItem> seq) {
		APICall theCall = null;
		for(int j = startIndex + 1; j < pattern.size(); j++) {
			APISeqItem next = pattern.get(j);
			if(next instanceof APICall && !((APICall)next).condition.equals("true")) {
				theCall = (APICall)next;
				break;
			}
		}
		
		
		String guard = null;
		if(theCall != null) {
			// find the same API call in the snippet to contextualize the guard condition
			APICall counterpart = findCounterpartCall(theCall, seq);
			if(counterpart != null) {
				guard = theCall.condition;
				
				// contextualize the receiver name in the API call
				if(guard.contains("rcv")) {
					guard.replaceAll("rcv", counterpart.receiver);
				}
				
				// TODO: contextualize the argument names in the API call
			}
		}
		
		return guard;
	}
	
	private APICall findCounterpartCall(APICall call, ArrayList<APISeqItem> seq) {
		APICall counterpart = null;
		for(APISeqItem item : seq) {
			if(item instanceof APICall) {
				APICall other = (APICall)item;
				if(other.name.equals(call.name)) {
					counterpart = other;
					break;
				}
			}
		}
		
		return counterpart;
	}
}
