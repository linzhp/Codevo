package edu.ucsc.codevo.controller;

import java.util.Iterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.ucsc.codevo.model.SourceFileAnalyzer;
import edu.ucsc.codevo.view.DependencyView;

public class AnalyzingHandler implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			SourceFileAnalyzer analyzer = new SourceFileAnalyzer();
			IWorkbenchPage activePage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
			ISelection selection = activePage.getSelection();
			if (selection instanceof IStructuredSelection) {
				@SuppressWarnings("rawtypes")
				Iterator iterator = ((IStructuredSelection)selection).iterator();
				while (iterator.hasNext()) {
					Object element = iterator.next();
					if (element instanceof IJavaProject) {
						analyzer.add((IJavaProject)element);
					}
				}
			} else {
				IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
				for (IProject p : projects) {
					if (p.isNatureEnabled(JavaCore.NATURE_ID)) {
						analyzer.add(JavaCore.create(p));
					}
				}
			}
			DependencyView view = (DependencyView)activePage.showView(DependencyView.ID);
			view.setInput(analyzer.getGraph().getClassEntities());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

}
