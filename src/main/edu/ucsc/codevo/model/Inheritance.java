package edu.ucsc.codevo.model;

import org.eclipse.jdt.core.dom.IBinding;

public class Inheritance {
	public IBinding subclass;
	public IBinding superclass;
	
	public Inheritance(IBinding subclass, IBinding superclass) {
		this.subclass = subclass;
		this.superclass = superclass;
	}
	
	@Override
	public String toString() {
		return subclass.getKey() + "-|>" + superclass.getKey();
	}
}
