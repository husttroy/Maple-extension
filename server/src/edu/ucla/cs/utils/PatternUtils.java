package edu.ucla.cs.utils;

import java.util.ArrayList;

import edu.ucla.cs.model.APICall;
import edu.ucla.cs.model.APISeqItem;
import edu.ucla.cs.model.ControlConstruct;

public class PatternUtils {
	public static ArrayList<APISeqItem> convert(String pattern) {
		String[] strSeqItems = pattern.split(";");
		ArrayList<APISeqItem> patternArray = new ArrayList<APISeqItem>();
		for (String strItem : strSeqItems) {
            // instantiate either a ControlConstruct or
            // an APICallItem and add to currentPattern
        	strItem = strItem.trim();
            switch (strItem) {
                case "try {":
                    patternArray.add(ControlConstruct.TRY);
                    break;
                case "catch {":
                    patternArray.add(ControlConstruct.CATCH);
                    break;
                case "finally {":
                    patternArray.add(ControlConstruct.FINALLY);
                    break;
                case "if {":
                    patternArray.add(ControlConstruct.IF);
                    break;
                case "else {":
                    patternArray.add(ControlConstruct.ELSE);
                    break;
                case "loop {":
                	patternArray.add(ControlConstruct.LOOP);
                	break;
                case "}":
                	patternArray.add(ControlConstruct.END_BLOCK);
                	break;
                default: 
                    String name = strItem.substring(0, strItem.indexOf('('));
                    int argCount = Integer.parseInt(strItem.substring(strItem.indexOf('(') + 1, strItem.indexOf(')')));
                    String guard = strItem.substring(strItem.indexOf('@') + 1);
                    patternArray.add(new APICall(name, guard, argCount));
            }
        }
		
		return patternArray;
	}
}
