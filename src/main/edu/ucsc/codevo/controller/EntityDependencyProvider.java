package edu.ucsc.codevo.controller;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;

import edu.ucsc.codevo.model.Entity;

public class EntityDependencyProvider extends ArrayContentProvider 
	implements IGraphEntityContentProvider{

	@Override
	public Object[] getConnectedTo(Object entity) {
		if (entity instanceof Entity) {
			return ((Entity)entity).getReferences();
		} else {
			throw new RuntimeException("Type not supported");
		}
	}

}
