package edu.ucla.cs.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import edu.ucla.cs.model.APICall;
import edu.ucla.cs.model.APISeqItem;
import edu.ucla.cs.model.ControlConstruct;

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
}
