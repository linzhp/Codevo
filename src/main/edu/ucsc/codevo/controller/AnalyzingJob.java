package edu.ucsc.codevo.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.ucsc.codevo.Activator;
import edu.ucsc.codevo.model.GraphRevision;
import edu.ucsc.codevo.model.ProjectReconfigurer;
import edu.ucsc.codevo.model.SourceFileAnalyzer;
import edu.ucsc.codevo.model.TimeMachine;

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
				GraphRevision graphRevisions = new GraphRevision();
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
					graphRevisions.addRevision(revision, analyzer.getEntities(), analyzer.getReferences(), analyzer.getInheritances());
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
