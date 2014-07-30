package edu.ucsc.codevo.model;

import org.eclipse.jdt.core.dom.IBinding;

public class Reference {
	public IBinding source;
	public IBinding target;

	public Reference(IBinding source, IBinding target) {
		this.source = source;
		this.target = target;
	}

	@Override
	public String toString() {
		return source.getKey() + "-->" + target.getKey();
	}
}
