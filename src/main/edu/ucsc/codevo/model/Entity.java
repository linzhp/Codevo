package edu.ucsc.codevo.model;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.IBinding;

public class Entity {
	public IBinding binding;
	private HashMap<Entity, Integer> references;

	public Entity(IBinding binding) {
		this.binding = binding;
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

	public String getName() {
		return binding.getName();
	}

	@Override
	public String toString() {
		return binding.getKey();
	}
}
