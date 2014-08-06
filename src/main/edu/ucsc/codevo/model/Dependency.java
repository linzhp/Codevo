package edu.ucsc.codevo.model;

import org.eclipse.jdt.core.dom.IBinding;

public class Dependency {
	public IBinding source;
	public IBinding target;

	public Dependency(IBinding source, IBinding target) {
		this.source = source;
		this.target = target;
	}

	@Override
	public String toString() {
		return source.getKey() + "-->" + target.getKey();
	}
}
