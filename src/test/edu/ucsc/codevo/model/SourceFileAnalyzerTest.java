package edu.ucsc.codevo.model;


import static org.hamcrest.collection.IsArrayContaining.hasItemInArray;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.hamcrest.CustomMatcher;
import org.junit.BeforeClass;
import org.junit.Test;

public class SourceFileAnalyzerTest {

	private static SourceFileAnalyzer analyzer;

	@BeforeClass
	public static void parseProject() {
		analyzer = new SourceFileAnalyzer();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		try {
			IWorkspaceRoot root = workspace.getRoot();
			IProject project = root.getProject("fixtureProject");
			if (!project.exists()) {
				project.create(null);	
			}
			project.open(null);
			
			IProjectDescription description = project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID });
			project.setDescription(description, null);
			
			IJavaProject javaProject = JavaCore.create(project);
			
			List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
			IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
			LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
			for (LibraryLocation element : locations) {
			 entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
			}
			FileUtils.copyDirectoryToDirectory(new File("fixtures/src"), project.getLocation().toFile());
			project.refreshLocal(IProject.DEPTH_INFINITE, null);
			entries.add(JavaCore.newSourceEntry(project.getFullPath().append("src")));
			
			javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
			
			IPackageFragment[] packages = javaProject.getPackageFragments();
			for (IPackageFragment p : packages) {
				if (p.getKind() == IPackageFragmentRoot.K_SOURCE) {
					for (ICompilationUnit unit : p.getCompilationUnits()) {
						ASTParser parser = ASTParser.newParser(AST.JLS4);
						parser.setSource(unit);
						parser.setResolveBindings(true);
						@SuppressWarnings("rawtypes")
						Hashtable options = JavaCore.getOptions();
						JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
						parser.setCompilerOptions(options);
						ASTNode ast = parser.createAST(null);
						ast.accept(analyzer);
					}
				}
			}
		} catch (IOException | CoreException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
	}

	@Test
	public void shouldGetClassName() {
		String[] vertices = getVertices();
		assertThat(vertices, hasItemInArray("Ledu/ucsc/codevo/fixtures/App;"));
		assertThat(vertices, hasItemInArray("Ledu/ucsc/codevo/fixtures/Dependency;"));
		assertThat(vertices, hasItemInArray("Ledu/ucsc/codevo/fixtures/BigCat;"));
	}
	
	@Test
	public void shouldGetMethodName() {
		String[] vertices = getVertices();
		assertThat(vertices, hasItemInArray("Ledu/ucsc/codevo/fixtures/App;.main([Ljava/lang/String;)V"));
	}
	
	@Test
	public void shouldGetFieldName() {
		assertThat(getVertices(), hasItemInArray("Ledu/ucsc/codevo/fixtures/App;.mode)I"));
	}
	
	@Test
	public void shouldGetConstructor() {
		String[] vertices = getVertices();
		assertThat(vertices, hasItemInArray("Ledu/ucsc/codevo/fixtures/App;.()V"));
		assertThat(vertices, hasItemInArray("Ledu/ucsc/codevo/fixtures/App;.(Ljava/lang/String;)V"));
	}
	
	@Test
	public void shouldGetInnerClassName() {
		assertThat(getVertices(), hasItemInArray("Ledu/ucsc/codevo/fixtures/App$Component;"));		
	}

	@Test
	public void shouldGetInnerClassMethodName() {
		assertThat(getVertices(), hasItemInArray("Ledu/ucsc/codevo/fixtures/App$Component;.process()V"));		
	}
	
	@Test
	public void shouldGetSimpleTypeReference() {
		Dependency[] edges = getEdges();
		assertThat(edges,hasItemInArray(new HasTarget<>("Ljava/lang/String;")));
		assertThat(edges,hasItemInArray(new HasTarget<>("Ljava/io/File;")));
	}

	@Test
	public void shouldGetQualifiedTypeReference() {
	}

	@Test
	public void shouldGetMethodInvocation() {
		Dependency[] edges = getEdges();
		assertThat(edges,hasItemInArray(new HasTarget<>("Ljava/io/PrintStream;.println(Ljava/lang/String;)V")));
	}

	@Test
	public void shouldGetFieldAccessInMethodInvocationReceiver() {
		Dependency[] edges = getEdges();
		assertThat(edges,hasItemInArray(new HasTarget<>("Ljava/lang/System;.out)Ljava/io/PrintStream;")));		
	}
	
	@Test
	public void shouldGetFieldAccessInFieldAccessReceiver() {
		Dependency[] edges = getEdges();
		assertThat(edges,hasItemInArray(new HasTarget<>(
				"Ledu/ucsc/codevo/fixtures/App$Component;.module)Ledu/ucsc/codevo/fixtures/App$Module;")));		
	}
	
	@Test
	public void shouldGetFieldAccess() {
		assertThat(getEdges(),hasItemInArray(new HasTarget<>(
				"Ledu/ucsc/codevo/fixtures/App$Module;.state)I")));
	}
	
	@Test
	public void shouldGetArrayQualifiedTypeReference() {
		Dependency[] edges = getEdges();
		assertThat(edges,hasItemInArray(new HasTarget<>("Ljava/io/IOException;")));
	}

	@Test
	public void shouldGetArrayTypeReference() {
		Dependency[] edges = getEdges();
		assertThat(edges,hasItemInArray(new HasTarget<>("Ljava/util/EventListener;")));
	}
	
	@Test
	public void shouldGetConstructorInvocation() {
		Dependency[] edges = getEdges();
		assertThat(edges, hasItemInArray(new HasTarget<>(
				"Ledu/ucsc/codevo/fixtures/App;.(Ljava/lang/String;)V")));
	}

	private String[] getVertices() {
		String[] vertices = analyzer.vertices.toArray(new String[analyzer.vertices.size()]);
		return vertices;
	}
	
	private Dependency[] getEdges() {
		return analyzer.edges.toArray(new Dependency[analyzer.edges.size()]);
	}
}

class HasTarget<T> extends CustomMatcher<T> {
	private String target;
	
	public HasTarget(String target) {
		super("a dependency on " + target);
		this.target = target;
	}

	@Override
	public boolean matches(Object item) {
		return ((Dependency)item).target.equals(target);
	}

}