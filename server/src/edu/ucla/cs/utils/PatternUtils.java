package edu.ucla.cs.utils;

import java.util.ArrayList;
import java.util.HashSet;

import edu.ucla.cs.model.APICall;
import edu.ucla.cs.model.APISeqItem;
import edu.ucla.cs.model.ControlConstruct;
import edu.ucla.cs.model.Pattern;

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
	

	public static HashSet<ArrayList<APISeqItem>> getAlternativePatterns(Pattern p) {
		HashSet<ArrayList<APISeqItem>> pset = new HashSet<ArrayList<APISeqItem>>();
		for(Pattern alter : p.alternative) {
			ArrayList<APISeqItem> pArray = PatternUtils.convert(p.pattern);
			pset.add(pArray);
			HashSet<ArrayList<APISeqItem>> pset2 = getAlternativePatterns(alter);
			pset.addAll(pset2);
		}
		return pset;
	}
	
	public static Pattern getThisOrAlternativePattern(Pattern p, ArrayList<APISeqItem> seq) {
		ArrayList<APISeqItem> pArray = PatternUtils.convert(p.pattern);
		if(pArray.equals(seq)) {
			return p;
		} else {
			for(Pattern alter : p.alternative) {
				Pattern p2 = getThisOrAlternativePattern(alter, seq);
				if(p2 != null) {
					return p2;
				}
			}
		}
		
		return null;
	}
}
