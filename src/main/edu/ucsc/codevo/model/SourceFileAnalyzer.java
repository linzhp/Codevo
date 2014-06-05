package edu.ucsc.codevo.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class SourceFileAnalyzer extends ASTVisitor {
	List<String> vertices = new ArrayList<>();
	List<Dependency> edges = new ArrayList<>();

	void recordDependency(ASTNode sourceNode, IBinding targetBinding) {
		if (targetBinding == null) {
			return;
		}
		while(sourceNode.getNodeType() != ASTNode.METHOD_DECLARATION &&
				sourceNode.getNodeType() != ASTNode.TYPE_DECLARATION) {
			sourceNode = sourceNode.getParent();
			if (sourceNode == null) {
				return;
			}
		}
		IBinding sourceBinding;
		switch (sourceNode.getNodeType()) {
		case ASTNode.METHOD_DECLARATION:
			sourceBinding = ((MethodDeclaration)sourceNode).resolveBinding();
			break;
		case ASTNode.TYPE_DECLARATION:
			sourceBinding = ((TypeDeclaration)sourceNode).resolveBinding();
			break;
		default:
			sourceBinding = null; // should not reach here
		}
		edges.add(new Dependency(sourceBinding.getKey(), targetBinding.getKey()));
	}
	
	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		vertices.add(typeDeclaration.resolveBinding().getKey());
		return true;
	}
	
	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		vertices.add(methodDeclaration.resolveBinding().getKey());
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		IVariableBinding b = node.resolveBinding();
		if (b != null) {
			vertices.add(b.getKey());			
		}
		return true;
	}

	@Override
	public boolean visit(SimpleType node) {
		ITypeBinding b = node.resolveBinding();
		recordDependency(node, b);
		// already get everything we need from this subtree
		return false;
	}
	
	@Override
	public boolean visit(FieldAccess node) {
		IVariableBinding b = node.resolveFieldBinding();
		recordDependency(node, b);
		return true;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding b = node.resolveMethodBinding();
		recordDependency(node, b);
		return true;
	}

	@Override
	public boolean visit(QualifiedName node) {
		IBinding b = node.resolveBinding();
		recordDependency(node, b);
		return true;
	}

	/*
	@Override
	public boolean visit(
			ArrayQualifiedTypeReference arrayQualifiedTypeReference,
			BlockScope scope) {
		edges.add(new Dependency(getQualifiedName(scope), Joiner.on('.').join(
				Utils.toStringArray(arrayQualifiedTypeReference.tokens))));
		return true;
	}

	@Override
	public boolean visit(
			ArrayQualifiedTypeReference arrayQualifiedTypeReference,
			ClassScope scope) {
		edges.add(new Dependency(getQualifiedName(scope), Joiner.on('.').join(
				Utils.toStringArray(arrayQualifiedTypeReference.tokens))));
		return true;
	}

	@Override
	public boolean visit(ArrayTypeReference arrayTypeReference, BlockScope scope) {
		edges.add(new Dependency(getQualifiedName(scope), new String(
				arrayTypeReference.token)));
		return true;
	}

	@Override
	public boolean visit(ArrayTypeReference arrayTypeReference, ClassScope scope) {
		edges.add(new Dependency(getQualifiedName(scope), new String(
				arrayTypeReference.token)));
		return true;
	}

	@Override
	public boolean visit(ExplicitConstructorCall explicitConstructor,
			BlockScope scope) {
		// not process for now, postpone until method overloading is considered
		return true;
	}

	@Override
	public boolean visit(FieldReference fieldReference, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	@Override
	public boolean visit(FieldReference fieldReference, ClassScope scope) {
		return true; // do nothing by default, keep traversing
	}
*/
}
