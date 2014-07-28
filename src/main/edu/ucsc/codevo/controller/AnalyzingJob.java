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
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import edu.ucsc.codevo.Activator;
import edu.ucsc.codevo.model.ProjectReconfigurer;
import edu.ucsc.codevo.model.SourceFileAnalyzer;
import edu.ucsc.codevo.model.TimeMachine;
import edu.ucsc.codevo.view.DependencyView;

public class AnalyzingJob extends Job {
	private ISelection selection;

	public AnalyzingJob(ISelection selection) {
		super("Analyzing Job");
		this.selection = selection;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected IStatus run(IProgressMonitor monitor) {
		try {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				Iterator<IJavaElement> iterator = structuredSelection.iterator();
				ArrayList<File> paths = new ArrayList<>();
				while (iterator.hasNext()) {
					paths.add(iterator.next().getResource().getLocation().toFile());
				}
				iterator = structuredSelection.iterator();
				IJavaProject javaProject = iterator.next().getJavaProject();
				TimeMachine timeMachine = new TimeMachine(paths);
				monitor.beginTask("Walking through revisions", timeMachine.getTotalRevsions());
				RevCommit revision = timeMachine.next();
				while (revision != null) {
					monitor.subTask("Reconfiguring project for revision " + revision.getName());
					ProjectReconfigurer.reconfigure(javaProject, monitor);
					monitor.subTask("Analyzing source code for revision " + revision.getName());
		            // reset iterator
					iterator = structuredSelection.iterator();
					monitor.beginTask("Analyzing selection", structuredSelection.size());
					SourceFileAnalyzer analyzer = new SourceFileAnalyzer(revision.getName());
					while (iterator.hasNext()) {
						IJavaElement element = iterator.next();
						monitor.subTask(element.getElementName());
						if (element instanceof IJavaProject) {
							analyzer.add((IJavaProject)element);
						} else if (element instanceof IPackageFragmentRoot) {
							analyzer.add((IPackageFragmentRoot)element);
						} else if (element instanceof IPackageFragment) {
							analyzer.add((IPackageFragment)element);
						}
					}
					System.out.println("Dependencies found: " + analyzer.getEdges().length);
					System.out.println("Entities found: " + analyzer.getVertices().length);
					monitor.worked(1);
					revision = timeMachine.next();
				}
				
			}
//			Display.getDefault().asyncExec(new Runnable() {
//
//				@Override
//				public void run() {
//					GraphInput graph = new GraphInput(analyzer.getVertices(), analyzer.getEdges());
//					try {
//						DependencyView view = (DependencyView)PlatformUI.getWorkbench().
//								getActiveWorkbenchWindow().getActivePage().
//								showView(DependencyView.ID);
//						view.setInput(graph.getClassEntities());
//					} catch (PartInitException e) {
//						e.printStackTrace();
//					}
//				}
//			});
		} catch (CoreException | IOException | GitAPIException e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Aborted with error", e);
		}
		return Status.OK_STATUS;
	}
	

}
