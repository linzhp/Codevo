package edu.ucsc.codevo.controller;

import java.util.HashMap;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.*;

import edu.ucsc.codevo.Utils;
import edu.ucsc.codevo.model.Reference;
import edu.ucsc.codevo.model.Entity;

public class GraphInput {
	private IBinding[] vertices;
	private Reference[] edges;
	private Entity[] methodEntities, classEntities;

	public Entity[] getClassEntities() {
		if (classEntities == null) {
			HashMap<String, Entity> entities = new HashMap<>();
			for (IBinding b : vertices) {
				ITypeBinding classBinding = getClass(b);
				String key = classBinding.getKey();
				if (!entities.containsKey(key)) {
					// the entity is a class
					entities.put(key, new Entity(classBinding));
				}
			}
			for (Reference d : edges) {
				ITypeBinding targetClassBinding = getClass(d.target);
				Entity targetEntity = null;
				if (targetClassBinding != null) {
					targetEntity = entities.get(targetClassBinding.getKey());
				}
				if (targetEntity != null) {
					Entity sourceEntity = entities.get(getClass(d.source).getKey());
					if (sourceEntity == null) {
						Utils.log(Status.ERROR, "No entity found for binding: " + d.source.getKey());
					} else if (sourceEntity != targetEntity){
						sourceEntity.addReference(targetEntity);
					}
				}
			}
			classEntities = entities.values().toArray(new Entity[entities.size()]);
		}
		return classEntities;
	}

	private ITypeBinding getClass(IBinding b) {
		switch (b.getKind()) {
		case IBinding.VARIABLE:
			return ((IVariableBinding)b).getDeclaringClass();
		case IBinding.METHOD:
			return ((IMethodBinding)b).getDeclaringClass();
		case IBinding.PACKAGE:
			return null;
		default:
			return (ITypeBinding)b;
		}
	}

	public Entity[] getMethodEntities() {
		if (methodEntities == null) {
			HashMap<String, Entity> entities = new HashMap<>();
			for (IBinding v : vertices) {
				entities.put(v.getKey(), new Entity(v));
			}
			for (Reference e : edges) {
				Entity targetEntity = entities.get(e.target.getKey());
				if (targetEntity != null) {
					Entity sourceEntity = entities.get(e.source.getKey());
					if (sourceEntity == null) {
						Utils.log(Status.ERROR, "No entity found for key: " + e.source.getKey());
					} else {
						sourceEntity.addReference(targetEntity);
					}
				}
			}
			methodEntities = entities.values().toArray(new Entity[entities.size()]);
		}
		return methodEntities;
	}

	public GraphInput(IBinding[] vertices, Reference[] edges) {
		this.vertices = vertices;
		this.edges = edges;
	}


}
