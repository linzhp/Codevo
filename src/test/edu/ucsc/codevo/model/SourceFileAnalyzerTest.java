package edu.ucsc.codevo.model;


import static org.hamcrest.collection.IsArrayContaining.hasItemInArray;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.junit.Test;

public class SourceFileAnalyzerTest {

	@Test
	public void shouldGetClassName() {
		String[] vertices = getVertices();
		assertThat(vertices, hasItemInArray("Ledu/ucsc/codevo/fixtures/App;"));
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
		
	}
	/*	
	@Test
	public void shouldGetArrayQualifiedTypeReference() throws IOException {
		Dependency[] edges = getEdges();
		assertThat(edges,hasItemInArray(new HasTarget<>("edu.ucsc.cs.netEvo.CodeEntity")));
	}

	@Test
	public void shouldGetArrayTypeReference() throws IOException {
		Dependency[] edges = getEdges();
		assertThat(edges,hasItemInArray(new HasTarget<>("String")));
	}
*/
	private String[] getVertices() {
		SourceFileAnalyzer analyzer = parseProject();
		String[] vertices = analyzer.vertices.toArray(new String[analyzer.vertices.size()]);
		return vertices;
	}
	
	private Dependency[] getEdges() {
		SourceFileAnalyzer analyzer = parseProject();
		return analyzer.edges.toArray(new Dependency[analyzer.edges.size()]);
	}


	private SourceFileAnalyzer parseProject() {
		SourceFileAnalyzer analyzer = new SourceFileAnalyzer();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		try {
			IWorkspaceRoot root = workspace.getRoot();
			IProject project = root.getProject("fixtureProject");
			if (!project.exists()) {
				project.create(null);				
				project.open(null);
			}
			
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
						parser.setKind(ASTParser.K_COMPILATION_UNIT);
						parser.setSource(unit);
						parser.setResolveBindings(true);
						ASTNode ast = parser.createAST(null);
						ast.accept(analyzer);
					}
				}
			}
		} catch (IOException | CoreException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		return analyzer;
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