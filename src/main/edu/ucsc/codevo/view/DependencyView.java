package edu.ucsc.codevo.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;

import edu.ucsc.codevo.controller.EntityDependencyProvider;
import edu.ucsc.codevo.controller.EntityLabelProvider;
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
		viewer.setLabelProvider(new EntityLabelProvider());
		setLayout();
		parent.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(final ControlEvent e) {
				viewer.applyLayout();
			}
		});
	}

	public void setInput(Entity[] entities) {
		viewer.setInput(entities);
		viewer.applyLayout();
	}

	public void setLayout(LayoutAlgorithm layout) {
		viewer.setLayoutAlgorithm(layout, true);
	}

	private void setLayout() {
		setLayout(new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING));
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
