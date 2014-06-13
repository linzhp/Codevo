package edu.ucsc.codevo.model;

import java.util.ArrayList;
import java.util.List;

public class Entity {
	String key;
	public String name;
	List<Entity> references;
	
	public Entity(String key, String name) {
		this.key = key;
		this.name = name;
		references = new ArrayList<>();
	}
	
	public Entity[] getReferences() {
		return references.toArray(new Entity[references.size()]);
	}
	
	@Override
	public String toString() {
		return key;
	}
}
