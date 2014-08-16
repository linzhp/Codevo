package edu.ucsc.codevo.model;

import org.eclipse.jdt.core.dom.ASTNode;

public class BindingFailureException extends RuntimeException {

	private static final long serialVersionUID = 6606338510554864800L;
	private ASTNode node;
	
	public BindingFailureException(ASTNode node) {
		this.node = node;
	}

	@Override
	public String getMessage() {
		return "Cannot resolve binding for " + node;
	}
}
