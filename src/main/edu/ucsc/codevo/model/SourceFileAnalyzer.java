package edu.ucsc.codevo.model;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import edu.ucsc.codevo.Utils;

public class SourceFileAnalyzer extends ASTVisitor {
	List<IBinding> entities = new ArrayList<>();
	List<Dependency> references = new ArrayList<>();
	List<Dependency> inheritances = new ArrayList<>();
	private String revision;
	
	public SourceFileAnalyzer(String revision) {
		this.revision = revision;
	}
	
	private void log(int level, String message) {
		Utils.log(level, "Revision " + revision + "\n" + message);
	}

	public void add(IJavaProject project) throws JavaModelException {
		IPackageFragment[] packages = project.getPackageFragments();
		for (IPackageFragment p : packages) {
			add(p);
		}
	}

	public void add(IPackageFragment p) throws JavaModelException {
		if (p.getKind() == IPackageFragmentRoot.K_SOURCE) {
			for (ICompilationUnit unit : p.getCompilationUnits()) {
				ASTParser parser = ASTParser.newParser(AST.JLS8);
				parser.setSource(unit);
				parser.setResolveBindings(true);
				@SuppressWarnings("rawtypes")
				Hashtable options = JavaCore.getOptions();
				JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
				parser.setCompilerOptions(options);
				ASTNode ast = parser.createAST(null);
				ast.accept(this);
			}
		}
	}

	public void add(IPackageFragmentRoot sourceFolder) throws JavaModelException {
		IJavaElement[] packages = sourceFolder.getChildren();
		for (IJavaElement e : packages) {
			if (e instanceof IPackageFragment) {
				add((IPackageFragment) e);
			} else {
				log(Status.WARNING,
						e.getElementName() + " is not a package fragment");
			}
		}
	}

	public IBinding[] getEntities() {
		return entities.toArray(new IBinding[entities.size()]);
	}

	public Dependency[] getReferences() {
		return references.toArray(new Dependency[references.size()]);
	}
	
	public Dependency[] getInheritances() {
		return inheritances.toArray(new Dependency[inheritances.size()]);
	}

	/**
	 *
	 * @param sourceNode
	 * @param targetBinding can be a local type. However, this dependency
	 * will be ignored eventually, as local types are not added into vertices
	 * @throws BindingFailureException 
	 */
	void recordDependency(ASTNode sourceNode, IBinding targetBinding) {
		if (targetBinding == null) {
			throw new BindingFailureException(sourceNode);
		}
		sourceNode = getSourceNode(sourceNode);
		if (sourceNode == null) {
			return;
		}
		
		IBinding sourceBinding;
		switch (sourceNode.getNodeType()) {
		case ASTNode.METHOD_DECLARATION:
			sourceBinding = ((MethodDeclaration)sourceNode).resolveBinding();
			break;
		case ASTNode.FIELD_DECLARATION:
		case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
		case ASTNode.ENUM_CONSTANT_DECLARATION:
			sourceBinding = ((AbstractTypeDeclaration)sourceNode.getParent()).resolveBinding();
			break;
		case ASTNode.TYPE_DECLARATION:
		case ASTNode.ENUM_DECLARATION:
		case ASTNode.ANNOTATION_TYPE_DECLARATION:
			sourceBinding = ((AbstractTypeDeclaration)sourceNode).resolveBinding();
			break;
		default:
			sourceBinding = null; // should not reach here
			throw new RuntimeException("Unknown source node type:\n" + sourceNode);
		}
		if (sourceBinding == null) {
			throw new BindingFailureException(sourceNode);
		}
		references.add(new Dependency(sourceBinding, targetBinding));
	}

	private ASTNode getSourceNode(ASTNode node) {
		while (!isGlobal(node)) {
			node = node.getParent();
			if (node == null) {
				return null;
			}
		}
		return node;
	}

	private boolean isGlobal(ASTNode node) {
		return node instanceof AbstractTypeDeclaration &&
				!((AbstractTypeDeclaration)node).isLocalTypeDeclaration() ||
				// excluding super type, type parameters, whose parents are also AbstractTypeDeclaration
				(node instanceof FieldDeclaration ||
						node instanceof MethodDeclaration ||
						node instanceof AnnotationTypeDeclaration ||
						node instanceof EnumConstantDeclaration) &&
						node.getParent() instanceof AbstractTypeDeclaration &&
						!((AbstractTypeDeclaration)node.getParent()).isLocalTypeDeclaration();
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		if (isGlobal(typeDeclaration)) {
			ITypeBinding b = typeDeclaration.resolveBinding();
			if (b != null) {
				entities.add(b);
			} else {
				throw new BindingFailureException(typeDeclaration);
			}
			Type superclass = typeDeclaration.getSuperclassType();
			if (superclass != null) {
				ITypeBinding superclassBinding = superclass.resolveBinding();
				if (superclassBinding == null) {
					throw new BindingFailureException(superclass);
				} else {
					inheritances.add(new Dependency(b, 
							superclassBinding));									
				}
			}
			@SuppressWarnings("unchecked")
			List<Type> interfaces = typeDeclaration.superInterfaceTypes();
			for (Type i : interfaces) {
				ITypeBinding interfaceBinding = i.resolveBinding();
				if (interfaceBinding == null) {
					throw new BindingFailureException(i);
				} else {
					inheritances.add(new Dependency(b, 
							interfaceBinding));						
				}
			}
		}
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		if (isGlobal(methodDeclaration)) {
			IMethodBinding b = methodDeclaration.resolveBinding();
			if (b != null) {
				entities.add(b);
			} else {
				throw new BindingFailureException(methodDeclaration);
			}
		}
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		if (isGlobal(node)) {
			for (Object o : node.fragments()) {
				IVariableBinding b =
						((VariableDeclarationFragment)o).resolveBinding();
				if (b != null) {
					entities.add(b);
				} else {
					throw new BindingFailureException(node);
				}
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
		if (isGlobal(node)) {
			ITypeBinding b = node.resolveBinding();
			if (b != null) {
				entities.add(b);
			} else {
				log(Status.WARNING, "Cannot resolve binding for\n" + node);
			}
		}
		return true;
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		if (isGlobal(node)) {
			IMethodBinding b = node.resolveBinding();
			if (b != null) {
				entities.add(b);
			} else {
				log(Status.WARNING, "Cannot resolve binding for\n" + node);
			}
		}
		return true;
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		if (isGlobal(node)) {
			IVariableBinding b = node.resolveVariable();
			if (b != null) {
				entities.add(b);
			} else {
				log(Status.WARNING, "Cannot resolve binding for\n" + node);
			}
		}
		return true;
	}

	//FIXME enum references cannot be resolved
	@Override
	public boolean visit(EnumDeclaration node) {
		if (isGlobal(node)) {
			ITypeBinding b = node.resolveBinding();
			if (b != null) {
				entities.add(b);
			} else {
				log(Status.WARNING, "Cannot resolve binding for\n" + node);
			}
		}
		return true;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		IBinding b = node.resolveAnnotationBinding().getAnnotationType();
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
		ITypeBinding b = node.resolveAnnotationBinding().getAnnotationType();
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
