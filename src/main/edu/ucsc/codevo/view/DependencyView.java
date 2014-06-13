package edu.ucsc.codevo.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

import edu.ucsc.codevo.controller.*;
import edu.ucsc.codevo.model.*;

public class DependencyView extends ViewPart {
	private GraphViewer viewer;
	public DependencyView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
	    viewer = new GraphViewer(parent, SWT.BORDER);
	    viewer.setContentProvider(new EntityDependencyProvider());
	    viewer.setLabelProvider(new EntityLabelProvider());
	    LayoutAlgorithm layout = setLayout();
	    viewer.setLayoutAlgorithm(layout, true);
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
	
	  private LayoutAlgorithm setLayout() {
	    LayoutAlgorithm layout;
	    // layout = new
	    // SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
	    layout = new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
	    // layout = new
	    // GridLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
	    // layout = new
	    // HorizontalTreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
	    // layout = new
	    // RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
	    return layout;

	  }
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
