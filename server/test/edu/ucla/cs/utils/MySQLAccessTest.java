package edu.ucla.cs.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import edu.ucla.cs.maple.server.MySQLAccess;
import edu.ucla.cs.model.Pattern;

public class MySQLAccessTest {

	@Test
	public void testGetPatternsWithAlternativePatterns() {
		MySQLAccess access = new MySQLAccess();
		access.connect();
		ArrayList<Pattern> plist = access.getPatterns("createNewFile");
		access.close();
		assertEquals(1, plist.size());
		assertEquals(1, plist.get(0).id);
		assertEquals(2, plist.get(0).alternative.size());
	}
}
