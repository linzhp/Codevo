package edu.ucsc.codevo.controller;

import java.util.HashMap;

import org.eclipse.core.runtime.Status;

import edu.ucsc.codevo.Utils;
import edu.ucsc.codevo.model.Dependency;
import edu.ucsc.codevo.model.Entity;

public class GraphInput {
	private Entity[] vertices;
	private Dependency[] edges;
	private Entity[] methodEntities, classEntities;

	public Entity[] getClassEntities() {
		if (classEntities == null) {
			HashMap<String, Entity> entities = new HashMap<>();
			for (Entity e : vertices) {
				String key = e.key;
				if (key.indexOf(';') == key.length() - 1) {
					// the entity is a class
					entities.put(key, e);
				}
			}
			for (Dependency d : edges) {
				String targetKey = d.target;
				Entity targetEntity = entities.get(targetKey.substring(0, targetKey.indexOf(';') + 1));
				if (targetEntity != null) {
					String sourceKey = d.source;
					Entity sourceEntity = entities.get(sourceKey.substring(0, sourceKey.indexOf(';') + 1));
					if (sourceEntity == null) {
						Utils.log(Status.ERROR, "No entity found for key: " + sourceKey);
					} else if (sourceEntity != targetEntity){
						sourceEntity.addReference(targetEntity);
					}
				}
			}
			classEntities = entities.values().toArray(new Entity[entities.size()]);
		}
		return classEntities;
	}

	public Entity[] getMethodEntities() {
		if (methodEntities == null) {
			HashMap<String, Entity> entities = new HashMap<>();
			for (Entity v : vertices) {
				entities.put(v.key, v);
			}
			for (Dependency e : edges) {
				Entity targetEntity = entities.get(e.target);
				if (targetEntity != null) {
					Entity sourceEntity = entities.get(e.source);
					if (sourceEntity == null) {
						Utils.log(Status.ERROR, "No entity found for key: " + e.source);
					} else {
						sourceEntity.addReference(targetEntity);
					}
				}
			}
			methodEntities = entities.values().toArray(new Entity[entities.size()]);
		}
		return methodEntities;
	}

	public GraphInput(Entity[] vertices, Dependency[] edges) {
		this.vertices = vertices;
		this.edges = edges;
	}


}
