package edu.ucla.cs.maple.ghlinks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import edu.ucla.cs.maple.server.MySQLAccess;
import edu.ucla.cs.model.APICall;
import edu.ucla.cs.model.APISeqItem;
import edu.ucla.cs.model.ControlConstruct;

public class LinkExtractor {

    public static void main(String[] args) {
        String folderName;
        // add "links" column to the patterns table in database
        MySQLAccess dbAccess = new MySQLAccess();
        dbAccess.connect();
        dbAccess.addColumn("links");
        
        // walk through folder names in patterns-with-urls directory 
        // and check database for the API/method
        File pwuDir = new File("patterns-with-urls");
        
        for (final File folder : pwuDir.listFiles()) {
            folderName = folder.getName();
            
            // convert the patterns in pattern.txt to strings of the same
            // format as the ones in the database
            ArrayList<String> filePatterns = extractPatterns(folder);
                
            // query the database for each pattern
            for (int i=0; i < filePatterns.size()-1; i++) {
                // if this pattern related to this API is in the database, then use
                // the file pattern's index+1 to locate its corresponding links,
                // and add those to the database
                String className = folderName.substring(0, folderName.indexOf("."));
                String methodName = folderName.substring(folderName.indexOf(".")+1);
                
                if (dbAccess.patternExists(className, methodName, filePatterns.get(i))) {
                    // extract the links from the sample.txt file and add as a single string
                    // to the database
                    dbAccess.addValueToColumn(className, methodName, filePatterns.get(i),
                            "links", extractLinks(folder.listFiles()[i+1]));
                }
             }           
         }
        dbAccess.close();
    }
    
    private static ArrayList<String> extractPatterns(File _folder) {
        ArrayList<String> patterns = new ArrayList<String>();
        BufferedReader br = null;
        String line = null;
        String p;
        
        try {
            // just read the first file, which is pattern.txt
            br = new BufferedReader(new FileReader(_folder.listFiles()[0]));
            
            // skip the first line
            br.readLine();
            // convert each pattern to an API call sequence and add to the ArrayList
            while ((line = br.readLine()) != null)
            {
                p = line.substring(line.indexOf("[")+1, line.indexOf("]"));
                patterns.add(convertPattern(p));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
            br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return patterns;
    }
    
    // convert the patterns from pattern.txt to the same format as used in 
    // the database
    public static String convertPattern(String pattern) {
        String[] seqItems = pattern.split(",");
        StringBuilder callSeq = new StringBuilder();
        
        for (String str : seqItems) {
            switch(str) {
                case "TRY":
                    callSeq.append("try {;");
                    break;
                case "CATCH":
                    callSeq.append("catch {;");
                    break;
                case "FINALLY":
                    callSeq.append("finally {;");
                    break;
                case "IF":
                    callSeq.append("if {;");
                    break;
                case "ELSE":
                    callSeq.append("else {;");
                    break;
                case "LOOP":
                    callSeq.append("loop {;");
                    break;
                case "END_BLOCK":
                    callSeq.append("}");
                    break;
                
                // this is an APICall
                default: 
                    callSeq.append(str + ";");
            }
        }
        
        // the last character shouldn't be a semicolon
        if (callSeq.substring(callSeq.length() - 1).equals(";")) {
            callSeq.setLength(callSeq.length()-1);
        }
        
        return callSeq.toString();
    }
    
    private static String extractLinks(File samples) {
        // construct a string of the top three working URLs in this file, separated
        // by backslashes
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        String line = null;
        String url;
        String method;
        int count = 0;
        
        try {
            // just read the first file, which is pattern.txt
            br = new BufferedReader(new FileReader(samples));
            
            // skip the first line
            br.readLine();
            // get the URL and add to StringBuilder if it exists
            // break the loop once we have three working URLs
            while (((line = br.readLine()) != null) && (count < 3))
            {
                url = line.substring(line.indexOf("[")+1, line.indexOf("]"));
                // if URL works, append to sb
                if (urlExists(url)) {
                    count++;
                    // add the corresponding method (i.e. the one that the
                    // API call occurs in)
                    method = line.substring(line.lastIndexOf("[")+1, line.lastIndexOf("]"));
                    // use a backslash as a delimiter between urls
                    sb.append(url + "\\\\" + method + "\\\\");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
            br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    
    // from https://stackoverflow.com/questions/4177864/checking-if-a-url-exists-or-not
    private static boolean urlExists(String URLName){
        try {
          HttpURLConnection.setFollowRedirects(false);
          // note : you may also need
          //        HttpURLConnection.setInstanceFollowRedirects(false)
          HttpURLConnection con =
             (HttpURLConnection) new URL(URLName).openConnection();
          con.setRequestMethod("HEAD");
          return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
           e.printStackTrace();
           return false;
        }
      }  
}
