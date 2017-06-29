package edu.ucla.cs.tempo;

public class CodeSnippet {
    
    private String id;
    private String snippet;

    public String getId() {
        return id;
    }

    public void setId(String _id) {
        this.id = _id;
    }
    
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String _snippet) {
        this.snippet = _snippet;
    }
    
    public String toString() {
        return "ID: " + id + ", snippet: " + snippet;
    }
}