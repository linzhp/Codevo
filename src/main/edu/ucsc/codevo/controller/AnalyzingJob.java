package edu.ucsc.codevo.controller;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import edu.ucsc.codevo.model.SourceFileAnalyzer;
import edu.ucsc.codevo.view.DependencyView;

public class AnalyzingJob extends Job {
	private IWorkbenchPage activePage;

	public AnalyzingJob(IWorkbenchPage activePage) {
		super("Analyzing Job");
		this.activePage = activePage;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			final SourceFileAnalyzer analyzer = new SourceFileAnalyzer();
			ISelection selection = activePage.getSelection();
			if (selection instanceof IStructuredSelection) {
				monitor.beginTask("Analyzing selection", ((IStructuredSelection) selection).size());
				@SuppressWarnings("rawtypes")
				Iterator iterator = ((IStructuredSelection)selection).iterator();
				while (iterator.hasNext()) {
					Object element = iterator.next();
					monitor.subTask(((IJavaElement)element).getElementName());
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
						DependencyView view = (DependencyView)activePage.showView(DependencyView.ID);
						view.setInput(graph.getClassEntities());
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return Status.OK_STATUS;
	}

}
