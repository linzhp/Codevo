package edu.ucsc.codevo.model;

public class Dependency {
	public String source;
	public String target;

	public Dependency(String source, String target) {
		this.source = source;
		this.target = target;
	}

	@Override
	public String toString() {
		return source + "->" + target;
	}
}
