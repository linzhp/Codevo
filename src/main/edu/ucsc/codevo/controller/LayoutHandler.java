package edu.ucsc.codevo.controller;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.GridLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

import edu.ucsc.codevo.view.DependencyView;

public class LayoutHandler implements IHandler {

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
		DependencyView view = (DependencyView)HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().findView(DependencyView.ID);
		String algorithm = event.getParameter("Codevo.layoutAlgorithm");
		switch (algorithm) {
		case "tree":
			view.setLayout(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING));
			break;
		case "spring":
			view.setLayout(new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING));
			break;
		case "grid":
			view.setLayout(new GridLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING));
			break;
		case "horizontal tree":
			view.setLayout(new HorizontalLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING));
			break;
		case "radial":
			view.setLayout(new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING));
			break;
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
