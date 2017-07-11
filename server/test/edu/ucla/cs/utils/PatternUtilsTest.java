package edu.ucla.cs.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import edu.ucla.cs.model.APICall;
import edu.ucla.cs.model.APISeqItem;
import edu.ucla.cs.model.ControlConstruct;
import edu.ucla.cs.model.Pattern;

public class PatternUtilsTest {
	@Test
	public void testConvertPatternFromStringToArrayList() {
		String pattern = "try {; new FileInputStream(1)@true; }; catch {; }";
		ArrayList<APISeqItem> pArray = PatternUtils.convert(pattern);
		assertEquals(ControlConstruct.TRY, pArray.get(0));
		APICall expectedCall = new APICall("new FileInputStream", "true", 1);
		assertEquals(expectedCall, pArray.get(1));
		assertEquals(ControlConstruct.END_BLOCK, pArray.get(2));
		assertEquals(ControlConstruct.CATCH, pArray.get(3));
		assertEquals(ControlConstruct.END_BLOCK, pArray.get(4));
	}
	
	@Test
	public void testGetTheRightPatternFromSeqArray() {
		String pattern = "try {; new FileInputStream(1)@true; }; catch {; }";
		Pattern p = new Pattern(0, null, null, pattern, 0, new ArrayList<Pattern>(), null, 0);
		String pattern2 = "new FileInputStream(1)@arg0.exists()";
		Pattern p2 = new Pattern(0, null, null, pattern2, 0, new ArrayList<Pattern>(), null, 0);
		p.alternative.add(p2);
		
		ArrayList<APISeqItem> pArray = PatternUtils.convert(pattern2);
		Pattern p3 = PatternUtils.getThisOrAlternativePattern(p, pArray);
		assertEquals(p2, p3);
	}
}
