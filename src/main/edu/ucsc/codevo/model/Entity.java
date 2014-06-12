package edu.ucsc.codevo.model;

import java.util.ArrayList;
import java.util.List;

public class Entity {
	String key;
	List<Entity> references;
	
	public Entity(String key) {
		this.key = key;
		references = new ArrayList<>();
	}
}
