package edu.ucsc.codevo.controller;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.zest.core.viewers.*;
import org.eclipse.zest.core.widgets.ZestStyles;

import edu.ucsc.codevo.model.Entity;

public class GraphStyleProvider extends LabelProvider
implements IEntityStyleProvider, IEntityConnectionStyleProvider {
	@Override
	public String getText(Object element) {
		if (element instanceof Entity) {
			return ((Entity)element).getName();
		} else {
			return "";
		}
	}

	@Override
	public Color getNodeHighlightColor(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getBorderColor(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getBorderHighlightColor(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getBorderWidth(Object entity) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Color getBackgroundColour(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getForegroundColour(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFigure getTooltip(Object entity) {
		if (entity instanceof Entity) {
			return new Label(((Entity)entity).toString());
		} else {
			return new Label();
		}
	}

	@Override
	public boolean fisheyeNode(Object entity) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getConnectionStyle(Object src, Object dest) {
		return ZestStyles.CONNECTIONS_DIRECTED;
	}

	@Override
	public Color getColor(Object src, Object dest) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getHighlightColor(Object src, Object dest) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLineWidth(Object src, Object dest) {
		return (int)Math.log(((Entity)src).getReferencesTo(dest));
	}
}