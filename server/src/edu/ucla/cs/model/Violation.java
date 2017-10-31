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
	            message = "You may want to check whether " + findGuardCondition(vioPattern) + ". " + vioPattern.support 
	                    + " GitHub code examples also do this.\n"
	                    + vioPattern.description;
	            break;
	        case MissingMethodCall:
	            message = "You may want to call " + ((APICall) item).getName() + "() " + 
                    missingAPIBeforeFocal(vioPattern) + " calling "  + vioPattern.support +
	                "(). Github code examples also do this.\n" + vioPattern.description;
	            break;
	        case MissingStructure:
	            message = "You may want to " + getControlStructureType(vioPattern) + " here. " 
	                    + vioPattern.support + " Github code examples also do this.\n" 
	                    + vioPattern.description;
	            break;
	        case DisorderMethodCall:
	            //TODO: make sure this is right
    	        message = "You may want to call " + ((APICall) item).getName() + "() " + 
                        missingAPIBeforeFocal(vioPattern) + " calling "  + vioPattern.support +
                        "(). Github code examples also do this.\n" + vioPattern.description;
                break;
	        case DisorderStructure:
	            // TODO: find out what order
	            message = "You may want to call " + getControlStructureType(vioPattern) + " in a different order. "
	                + vioPattern.support + "GitHub code examples also do this.\n"
	                + vioPattern.description;
	            break;
	    }
	    return message;
	}
	
	private String getControlStructureType(Pattern vioPattern) {
	    String structure;
        if (item == ControlConstruct.TRY) {
            structure = "handle the potential exception thrown by " 
                + vioPattern.methodName + "() by using a try-catch block";
        }
        else if (item instanceof CATCH) {
            structure = "handle the potential exception " + ((CATCH) item).type 
                    + " thrown by " + vioPattern.methodName + "() by using a try-catch block";
        }
        else if (item == ControlConstruct.FINALLY) {
            structure = "call " + vioPattern.methodName + "() in a finally block to ensure it is "
                + "always called at the end in case of potential exceptions";
        }
        else if (item == ControlConstruct.IF) {
            // TODO: find guard condition
            structure = "use an if-statement";
        }
        // I would like to use an else violation for if-else clauses
        // to keep them distinct from solo if-statements
        else if (item == ControlConstruct.ELSE) {
            // TODO: find guard condition
            structure = "use an if-else statement";
        }
        else if (item == ControlConstruct.LOOP) {
            // TODO: find guard condition
            structure = "use a loop";
        }
        else {
            // I'm not sure what the default should be.
            structure = "use a control structure";
        }
        
        return structure;
	}
	
	private String missingAPIBeforeFocal(Pattern vioPattern) {
	    // missing API call: ((APICall) item)
	    // focal API call: vioPattern.className + vioPattern.methodName
	    // find if missing API is before focal in vioPattern.pattern
	    String ret;
	    if (vioPattern.pattern.indexOf(((APICall) item).getName()) 
	        < vioPattern.pattern.indexOf(vioPattern.methodName)) {
	        ret = "before";
	    }
	    else {
	        ret = "after";
	    }
	        
	    return ret;
	}
	
	private String findGuardCondition(Pattern vioPattern) {
	    String guard = "";
	    String[] p = vioPattern.pattern.split(",");
	    for (String str : p) {
	        System.out.println(str);
	        if (str.contains("@")) {
	            guard = str.substring(str.indexOf("@"));
	            System.out.println(guard);
	            break;
	        }
	    }
	    
	    String ret = "";
	    switch (guard) {
	        //TODO: make this more dynamic
	        case "@rcv!=null": ret = "the receiver of " + ((APICall) item).name + " is not null";
	            break;
	        case "@rcv.exists()": ret = ((APICall) item).name + " exists";
	            break;
	        default: ret = "a condition is true";
	    }
	    
	    return ret;
	}
}
