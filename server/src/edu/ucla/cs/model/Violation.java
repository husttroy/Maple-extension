package edu.ucla.cs.model;

public class Violation {
	public ViolationType type;
	public APISeqItem item; 
	public String id;
	
	public Violation(ViolationType type, APISeqItem item) {
		this.type = type;
		this.item = item;
	}
	
	public String getViolationMessage(Pattern vioPattern) {
	    String message = null;
	    switch (type) {
	        case IncorrectPrecondition:
	            message = "You may want to check whether a condition is true before calling "
	                    + ((APICall) item).getName() + "(). " + vioPattern.support 
	                    + " GitHub code examples also do this.\n"
	                    + vioPattern.description;
	            break;
	        case MissingMethodCall:
	            message = "You may want to call " + ((APICall) item).getName() + "() here. " + vioPattern.support +
	                " Github code examples also do this.\n" + vioPattern.description;
	            break;
	        case MissingStructure:
	            message = "You may want to use " + getControlStructureType() + " here. " 
	                    + vioPattern.support + " Github code examples also do this.\n" 
	                    + vioPattern.description;
	            break;
	        case DisorderMethodCall:
    	        message = "You may want to call " + ((APICall) item).getName() + "() in a different order. "
        	        + vioPattern.support + "GitHub code examples also do this.\n"
        	        + vioPattern.description;
                break;
	        case DisorderStructure:
	            message = "You may want to call " + getControlStructureType() + " in a different order. "
	                + vioPattern.support + "GitHub code examples also do this.\n"
	                + vioPattern.description;
	            break;
	    }
	    return message;
	}
	
	private String getControlStructureType() {
	    String structure;
        if (item == ControlConstruct.TRY || item == ControlConstruct.CATCH) {
            structure = "a try-catch block";
        }
        else if (item == ControlConstruct.FINALLY) {
            //TODO: is it necessary to separate this from try-catch?
            structure = "a try-catch-finally block";
        }
        else if (item == ControlConstruct.IF) {
            structure = "an if-statement";
        }
        // I would like to use an else violation for if-else clauses
        // to keep them distinct from solo if-statements
        else if (item == ControlConstruct.ELSE) {
            structure = "an if-else statement";
        }
        else if (item == ControlConstruct.LOOP) {
            structure = "a loop";
        }
        else {
            // I'm not sure what the default should be.
            structure = "a control structure";
        }
        
        return structure;
	}
}
