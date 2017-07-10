package edu.ucla.cs.model;

import java.util.ArrayList;

public class Pattern {
	public int id;
	public String className;
	public String methodName;
	public String pattern;
	public int support;
	public ArrayList<Pattern> alternative;
	public String description;
	public int vote;

	public Pattern(int id, String className, String methodName, String pattern,
			int support, ArrayList<Pattern> alternative, String description, int vote) {
		this.id = id;
		this.className = className;
		this.methodName = methodName;
		this.pattern = pattern;
		this.support = support;
		this.alternative = alternative;
		this.description = description;
		this.vote = vote;
	}
}
