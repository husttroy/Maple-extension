package edu.ucla.cs.maple.server;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

public class MapleWebSocketHandlerTest {

    //CodeSnippet1: https://stackoverflow.com/questions/10506180/
    //CodeSnippet2: https://stackoverflow.com/questions/10065723/
    //CodeSnippet3: https://stackoverflow.com/questions/12100580/
    @Test
    public void testCodeSnippetAnalysis() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("test/CodeSnippet1"));
        String cs = br.readLine();
        MapleWebSocketHandler mwsh = new MapleWebSocketHandler();
        mwsh.onMessage(cs);
        // TODO: once communication with client is figured out, finish implementing
        //fail("Not yet implemented");
    }

}
