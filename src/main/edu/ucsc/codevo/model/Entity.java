package edu.ucsc.codevo.model;

import java.util.HashMap;

public class Entity {
	public String key;
	public String name;
	private HashMap<Entity, Integer> references;

	public Entity(String key, String name) {
		this.key = key;
		this.name = name;
		references = new HashMap<>();
	}

	public Entity[] getReferences() {
		return references.keySet().toArray(new Entity[references.size()]);
	}

	public void addReference(Entity target) {
		int weight;
		if (references.containsKey(target)) {
			weight = references.get(target) + 1;
		} else {
			weight = 1;
		}
		references.put(target, weight);
	}

	public int getReferencesTo(Object target) {
		if (references.containsKey(target)) {
			return references.get(target);
		} else {
			return 0;
		}
	}

	@Override
	public String toString() {
		return key;
	}
}
