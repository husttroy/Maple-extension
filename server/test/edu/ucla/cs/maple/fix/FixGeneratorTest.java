package edu.ucla.cs.maple.fix;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import edu.ucla.cs.maple.server.PartialProgramAnalyzer;
import edu.ucla.cs.model.APICall;
import edu.ucla.cs.model.APISeqItem;
import edu.ucla.cs.model.ControlConstruct;
import edu.ucla.cs.utils.FileUtils;

public class FixGeneratorTest {
	
	@Test
	public void testTryCatch() throws Exception {
		ArrayList<APISeqItem> pattern = new ArrayList<APISeqItem>();
		pattern.add(ControlConstruct.TRY);
		pattern.add(new APICall("new FileInputStream", "true", 1));
		pattern.add(ControlConstruct.END_BLOCK);
		pattern.add(ControlConstruct.CATCH);
		pattern.add(ControlConstruct.END_BLOCK);
		
		String path = "test/snippet_with_normal_type.txt";
		String snippet = FileUtils.readFileToString(path);
		PartialProgramAnalyzer ppa = new PartialProgramAnalyzer(snippet);
		HashMap<String, ArrayList<APISeqItem>> seqs = ppa.retrieveAPICallSequences();
		ArrayList<APISeqItem> seq = seqs.get("foo");
		
		FixGenerator fixGen = new FixGenerator();
		String fix = fixGen.generate(pattern, seq);
		System.out.println(fix);
	}
}
