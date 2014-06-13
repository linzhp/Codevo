package edu.ucsc.codevo.controller;

import org.eclipse.jface.viewers.LabelProvider;

import edu.ucsc.codevo.model.Entity;

public class EntityLabelProvider extends LabelProvider {
	  @Override
	  public String getText(Object element) {
	    if (element instanceof Entity) {
	    	return ((Entity)element).toString();
	    } else {
	    	return "";
	    }
	  }
	} 