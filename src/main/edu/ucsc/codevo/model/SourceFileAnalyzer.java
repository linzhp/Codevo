package edu.ucsc.codevo.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.*;

public class SourceFileAnalyzer extends ASTVisitor {
	List<String> vertices = new ArrayList<>();
	List<Dependency> edges = new ArrayList<>();

	void log(int level, String message) {
		ResourcesPlugin plugin = ResourcesPlugin.getPlugin();
		plugin.getBundle().getSymbolicName();
		ILog logger = plugin.getLog();
		logger.log(new Status(
				level, 
				plugin.getBundle().getSymbolicName(), 
				message));
	}
	
	void recordDependency(ASTNode sourceNode, IBinding targetBinding) {
		if (targetBinding == null) {
			log(Status.INFO, "Cannot resolve binding for " + sourceNode);
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
	public boolean visit(FieldDeclaration node) {
		for (Object o : node.fragments()) {
			IVariableBinding b = 
					((VariableDeclarationFragment)o).resolveBinding();
			if (b != null) {
				vertices.add(b.getKey());			
			}
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
	
	/**
	 * Can only handle the invocations between constructors,
	 * not class instance creation
	 */
	@Override
	public boolean visit(ConstructorInvocation node) {
		IBinding b = node.resolveConstructorBinding();
		recordDependency(node, b);
		return true;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		vertices.add(node.resolveBinding().getKey());
		return true;
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		vertices.add(node.resolveBinding().getKey());
		return true;
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		vertices.add(node.resolveVariable().getKey());
		return true;
	}

	//FIXME enum references cannot be resolved
	@Override
	public boolean visit(EnumDeclaration node) {
		vertices.add(node.resolveBinding().getKey());
		return true;
	}
	
	@Override
	public boolean visit(MarkerAnnotation node) {
		IBinding b = node.resolveAnnotationBinding();
		recordDependency(node, b);
		return true;
	}

	@Override
	public boolean visit(ParameterizedType node) {
		// type arguments are not recorded until it is actually used
		IBinding b = node.resolveBinding();
		recordDependency(node, b);
		return true;
	}

	@Override
	public boolean visit(QualifiedType node) {
		//FIXME don't know what a QualifiedType node is
		log(Status.WARNING, 
			"Finally find out what a QualifiedType is like\n" + node);
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		IBinding b = node.resolveAnnotationBinding();
		recordDependency(node, b);
		return true;
	}
	
	@Override
	public boolean visit(SuperConstructorInvocation node) {
		IBinding b = node.resolveConstructorBinding();
		recordDependency(node, b);
		return true;
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		IBinding b = node.resolveFieldBinding();
		recordDependency(node, b);
		return true;
	}
	
	@Override
	public boolean visit(SuperMethodInvocation node) {
		IBinding b = node.resolveMethodBinding();
		recordDependency(node, b);
		return true;
	}

}
