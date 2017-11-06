package edu.ucla.cs.model;

import java.util.ArrayList;

public class Violation {
	public ViolationType type;
	public APISeqItem item; 
	public String id;
	public String fix = "";
	public Pattern vioPattern;
	public ArrayList<Violation> redundancies; 
	
	public Violation(ViolationType type, APISeqItem item) {
		this.type = type;
		this.item = item;
		this.redundancies = new ArrayList<Violation>(); 
	}
	
	@Override
	public boolean equals(Object o) {
	    boolean isEqual = false;
	    
	    if (o instanceof Violation) {
	        if (this.fix.equals(((Violation) o).fix)) {
	            isEqual = true;
	        }
	    }
	    
	    return isEqual;
	}
	
	public void addRedundant(Violation other) {
	    redundancies.add(other);
	}
	
	public boolean hasRedundancies() {
	    return !redundancies.isEmpty();
	}
	
	public String getViolationMessage(Pattern _vioPattern) {
	    vioPattern = _vioPattern;
	    String message = "You may want to ";
	    message += getTypeMessage();
	    if (hasRedundancies()) {
	        for (Violation v : redundancies) {
	            v.vioPattern = _vioPattern;
	            message += "You may also want to " + v.getTypeMessage();
	        }
	    }
	    
	    message += vioPattern.support + " Github code examples also do this.\n" 
                + vioPattern.description;
	    return message;
	}
	
	private String getTypeMessage() {
	    String message = null;
        switch (type) {
            case IncorrectPrecondition:
                message = "check whether " + findGuardCondition() + ". ";
                break;
            case MissingMethodCall:
                message = "call " + ((APICall) item).getName() + "() " + 
                    missingAPIBeforeFocal() + " calling " + vioPattern.methodName + "(). ";
                break;
            case MissingStructure:
                message = getControlStructureType() + " here. ";
                break;
            case DisorderMethodCall:
                //TODO: make sure this is right
                message = "call " + ((APICall) item).getName() + "() " + 
                        missingAPIBeforeFocal() + " calling " + vioPattern.methodName + "(). ";
                break;
            case DisorderStructure:
                // TODO: find out what order
                message = "call " + getControlStructureType() + " in a different order. ";
                break;
        }
        return message;
	}
	
	private String getControlStructureType() {
	    String structure;
	    if (item instanceof CATCH) {
            structure = "handle the potential " + ((CATCH) item).type 
                    + " thrown by " + vioPattern.methodName + "() by using a try-catch block";
        }
        else if (item == ControlConstruct.FINALLY) {
            structure = "call " + vioPattern.methodName + "() in a finally block to ensure it is "
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
	
	private String missingAPIBeforeFocal() {
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
	
	private String findGuardCondition() {
	    String guard = "";
	    String[] p = vioPattern.pattern.split(",");
	    for (String str : p) {
	        //System.out.println(str);
	        if (str.contains("@")) {
	            guard = str.substring(str.indexOf("@"));
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
