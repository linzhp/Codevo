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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.IBinding;
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
			analyzer.add(javaProject);
		} catch (IOException | CoreException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
	}

	@Test
	public void shouldGetClassName() {
		IBinding[] vertices = getVertices();
		assertThat(vertices, hasItemInArray(new HasEntity<>(
				"Ledu/ucsc/codevo/fixtures/App;")));
		assertThat(vertices, hasItemInArray(new HasEntity<>(
				"Ledu/ucsc/codevo/fixtures/Dependency;")));
		assertThat(vertices, hasItemInArray(new HasEntity<>(
				"Ledu/ucsc/codevo/fixtures/BigCat;")));
	}

	@Test
	public void shouldGetMethodName() {
		assertThat(getVertices(), hasItemInArray(new HasEntity<>(
				"Ledu/ucsc/codevo/fixtures/App;.main([Ljava/lang/String;)V")));
	}

	@Test
	public void shouldGetFieldName() {
		assertThat(getVertices(), hasItemInArray(new HasEntity<>(
				"Ledu/ucsc/codevo/fixtures/App;.mode)I")));
	}

	@Test
	public void shouldGetConstructor() {
		IBinding[] vertices = getVertices();
		assertThat(vertices, hasItemInArray(new HasEntity<>(
				"Ledu/ucsc/codevo/fixtures/App;.()V")));
		assertThat(vertices, hasItemInArray(new HasEntity<>(
				"Ledu/ucsc/codevo/fixtures/App;.(Ljava/lang/String;)V")));
	}

	@Test
	public void shouldGetInnerClassName() {
		assertThat(getVertices(), hasItemInArray(new HasEntity<>(
				"Ledu/ucsc/codevo/fixtures/App$Component;")));
	}

	@Test
	public void shouldGetInnerClassMethodName() {
		assertThat(getVertices(), hasItemInArray(new HasEntity<>(
				"Ledu/ucsc/codevo/fixtures/App$Component;.process()V")));
	}

	@Test
	public void shouldGetSimpleTypeReference() {
		Dependency[] edges = getEdges();
		assertThat(edges,hasItemInArray(new HasTarget<>("Ljava/lang/String;")));
	}

	@Test
	public void shouldGetTypeReferenceFromLocalClass() {
		assertThat(getEdges(), hasItemInArray(new HasDependency<>(
				"Ledu/ucsc/codevo/fixtures/App;.run()V",
				"Ljava/io/File;")));
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
		assertThat(edges,hasItemInArray(new HasDependency<>(
				"Ledu/ucsc/codevo/fixtures/App;.main([Ljava/lang/String;)V",
				"Ledu/ucsc/codevo/fixtures/Dependency;")
				));
	}

	@Test
	public void shouldGetConstructorInvocation() {
		Dependency[] edges = getEdges();
		assertThat(edges, hasItemInArray(new HasTarget<>(
				"Ledu/ucsc/codevo/fixtures/App;.(Ljava/lang/String;)V")));
	}

	@Test
	public void shouldGetParameterizedType() {
		assertThat(
				getEdges(),
				hasItemInArray(new HasDependency<>(
						"Ledu/ucsc/codevo/fixtures/Dependency;.toString()Ljava/lang/String;",
						"Ledu/ucsc/codevo/fixtures/App$Component;")
						)
				);
	}

	private IBinding[] getVertices() {
		IBinding[] vertices = analyzer.vertices.toArray(
				new IBinding[analyzer.vertices.size()]);
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
		return ((Dependency)item).target.getKey().equals(target);
	}
}

class HasDependency<T> extends CustomMatcher<T> {
	private String source, target;

	public HasDependency(String source, String target) {
		super("a dependency from " + source + " to " + target);
		this.source = source;
		this.target = target;
	}

	@Override
	public boolean matches(Object item) {
		Dependency d = (Dependency)item;
		return d.source.getKey().equals(source) && d.target.getKey().equals(target);
	}

}

class HasEntity<T> extends CustomMatcher<T> {
	private String key;

	public HasEntity(String key) {
		super("a entity " + key);
		this.key = key;
	}

	@Override
	public boolean matches(Object item) {
		return ((IBinding)item).getKey().equals(key);
	}

}