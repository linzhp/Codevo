package edu.ucsc.codevo.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.taskdefs.Javac;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import edu.ucsc.codevo.model.SourceFileAnalyzer;
import edu.ucsc.codevo.view.DependencyView;

public class AnalyzingJob extends Job {
	private ISelection selection;

	public AnalyzingJob(ISelection selection) {
		super("Analyzing Job");
		this.selection = selection;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			final SourceFileAnalyzer analyzer = new SourceFileAnalyzer();
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				monitor.beginTask("Checkout from git", 1);
				@SuppressWarnings("rawtypes")
				Iterator iterator = structuredSelection.iterator();
				IJavaElement element = (IJavaElement)iterator.next();
				IResource resource = element.getResource();
				builder.findGitDir(resource.getLocation().toFile());
				Repository repository = builder.build();
				Git git = new Git(repository);
				git.checkout().setName("ad1756cae624e9540c909c6c2ddd6eff92c5e822").call();
				
				IProject eclipseProject = resource.getProject();
				monitor.beginTask("Configuring project with ant build file", 1);
				File buildFile = eclipseProject.getFile("build.xml").getLocation().toFile();
				Project antProject = new Project();
				antProject.init();
				antProject.setUserProperty(MagicNames.ANT_FILE,
                        buildFile.getAbsolutePath());
				antProject.setUserProperty(MagicNames.ANT_FILE_TYPE,
                        MagicNames.ANT_FILE_TYPE_FILE);
				ProjectHelper.configureProject(antProject, buildFile);
				Task[] tasks = antProject.getTargets().get("build").getTasks();
	            for (Task task : tasks) {                                                                                                                                                                            
	                if (task instanceof UnknownElement) {                                                                                                                                                            
	                    ((UnknownElement)task).maybeConfigure();                                                                                                                                                     
	                    task = ((UnknownElement)task).getTask();                                                                                                                                                     
	                    if (task == null) {                                                                                                                                                                          
	                        continue;                                                                                                                                                                               
	                    }                                                                                                                                                                                            
	                }                                                                                                                                                                                                
	                if (task instanceof Javac) {
	                	Javac javacTask = (Javac)task;
	        			IJavaProject javaProject = element.getJavaProject();
	                    configureClasspath(javaProject, javacTask, monitor);
	        			break;
	                }                                                                                                                                                                                                
	            }
				eclipseProject.refreshLocal(IProject.DEPTH_INFINITE, monitor);

	            // reset iterator
				iterator = structuredSelection.iterator();
				monitor.beginTask("Analyzing selection", structuredSelection.size());
				while (iterator.hasNext()) {
					element = (IJavaElement)iterator.next();
					monitor.subTask(element.getElementName());
					if (element instanceof IJavaProject) {
						analyzer.add((IJavaProject)element);
					} else if (element instanceof IPackageFragmentRoot) {
						analyzer.add((IPackageFragmentRoot)element);
					} else if (element instanceof IPackageFragment) {
						analyzer.add((IPackageFragment)element);
					}
					monitor.worked(1);
				}
			} else {
				IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
				monitor.beginTask("Analyzer projects in the workspace", projects.length);
				for (IProject p : projects) {
					if (p.isNatureEnabled(JavaCore.NATURE_ID)) {
						monitor.subTask(p.getName());
						analyzer.add(JavaCore.create(p));
						monitor.worked(1);
					}
				}
			}
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					GraphInput graph = new GraphInput(analyzer.getVertices(), analyzer.getEdges());
					try {
						DependencyView view = (DependencyView)PlatformUI.getWorkbench().
								getActiveWorkbenchWindow().getActivePage().
								showView(DependencyView.ID);
						view.setInput(graph.getClassEntities());
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (CoreException | IOException | GitAPIException e) {
			e.printStackTrace();
		}
		return Status.OK_STATUS;
	}
	
	private void configureClasspath(IJavaProject javaProject, Javac javacTask,
			IProgressMonitor monitor) throws CoreException, JavaModelException, IOException {
		ArrayList<IClasspathEntry> entries = new ArrayList<>();
		IProject eclipseProject = javaProject.getProject();
		// add source directories
		File destdir = javacTask.getDestdir();
		if (destdir.exists()) {
			FileUtils.cleanDirectory(destdir);
		}
		org.apache.tools.ant.types.Path sourceDirs = javacTask.getSrcdir();
		for (String srcDir : sourceDirs.list()) {
			IFolder folder = eclipseProject.getFolder(new File(srcDir).getName());
			folder.createLink(
					new Path(srcDir), 
					IResource.ALLOW_MISSING_LOCAL | IResource.REPLACE, 
					monitor);
			entries.add(JavaCore.newSourceEntry(folder.getFullPath()));
		}
		// add libraries
		org.apache.tools.ant.types.Path classpath = 
				javacTask.getClasspath();                                                                                                                                                               
		if (classpath != null) {
			for (String cp : classpath.list()) {
				File classpathEntry = new File(cp);
				IPath path = new Path(classpathEntry.getAbsolutePath());
				if (classpathEntry.isDirectory()) {
					IFolder folder = eclipseProject.getFolder(path.lastSegment());
					folder.createLink(
							path, 
							IResource.ALLOW_MISSING_LOCAL | IResource.REPLACE, 
							monitor);
				}
				entries.add(JavaCore.newLibraryEntry(
						path, null, null));
			}
		}
		// add JRE
		entries.add(
				JavaCore.newVariableEntry(
						new Path(JavaRuntime.JRELIB_VARIABLE), 
						new Path(JavaRuntime.JRESRC_VARIABLE), 
						new Path(JavaRuntime.JRESRCROOT_VARIABLE)));
		javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), monitor);
	}

}
