package edu.ucla.cs.model;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;


public class ViolationTest {

    @Test
    public void testGetViolationMessage() {
        Violation vio = new Violation(ViolationType.MissingStructure, 
                new APICall("write", "true", 1));
        Pattern p = new Pattern(0, "FileChannel", "write", "try {; write(1)@true; }; catch {; }",
                        1231, new ArrayList<Pattern>(), "Handle the potential IOException thrown by write.", 10);
        System.out.println(vio.getViolationMessage(p));
    }

}
