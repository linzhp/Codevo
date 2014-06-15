package edu.ucsc.codevo.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.*;

import edu.ucsc.codevo.controller.EntityDependencyProvider;
import edu.ucsc.codevo.controller.GraphStyleProvider;
import edu.ucsc.codevo.model.Entity;

public class DependencyView extends ViewPart {
	public final static String ID = "Codevo.dependencyView";
	private GraphViewer viewer;

	public DependencyView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new GraphViewer(parent, SWT.BORDER);
		viewer.setContentProvider(new EntityDependencyProvider());
		viewer.setLabelProvider(new GraphStyleProvider());
		setLayout();
	}

	public void setInput(Entity[] entities) {
		viewer.setInput(entities);
		viewer.applyLayout();
	}

	public void setLayout(LayoutAlgorithm layout) {
		viewer.setLayoutAlgorithm(layout, true);
	}

	private void setLayout() {
		setLayout(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING));
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
