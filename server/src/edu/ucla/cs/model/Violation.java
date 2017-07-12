package edu.ucla.cs.model;

public class Violation {
	public ViolationType type;
	public APISeqItem item; 
	
	public Violation(ViolationType type, APISeqItem item) {
		this.type = type;
		this.item = item;
	}
	
	// TODO: use vioPattern.methodName instead of item?
	public String getViolationMessage(Pattern vioPattern) {
	    String message = null;
	    switch (type) {
	        case IncorrectPrecondition:
	            message = "You may want to check whether a condition is true before calling "
	                    + item + ". " + vioPattern.support + " GitHub code examples also do this.\n"
	                    + vioPattern.description;
	            break;
	        case MissingMethodCall:
	            message = "You may want to call " + item + " here." + vioPattern.support +
	                " Github code examples also do this.\n" + vioPattern.description;
	            break;
	        case MissingStructure:
	            message = "You may want to call a control structure here. " + vioPattern.support +
	                " Github code examples also do this.\n" + vioPattern.description;
	            break;
	        case DisorderMethodCall:
    	        message = "You may want to call " + item + " in a different order. "
        	        + vioPattern.support + "GitHub code examples also do this.\n"
        	        + vioPattern.description;
                break;
	        case DisorderStructure:
	            message = "You may want to call " + item + " in a different order. "
	                + vioPattern.support + "GitHub code examples also do this.\n"
	                + vioPattern.description;
	            break;
	    }
	    return message;
	}
}
