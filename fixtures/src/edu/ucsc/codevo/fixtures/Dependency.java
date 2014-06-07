package edu.ucsc.codevo.fixtures;

public class Dependency {
	String source;
	String target;
	
	public Dependency(String source, String target) {
		this.source = source;
		this.target = target;
	}
	
	@Override
	public String toString() {
		return source + "->" + target;
	}
}
