package edu.ucsc.codevo.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jgit.revwalk.RevCommit;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import edu.ucsc.codevo.Utils;

public class GraphRevision {
	public void addRevision(
			RevCommit revision,
			List<IBinding> entities, 
			List<Dependency> references, 
			List<Dependency> inheritances) throws IOException {
		DB db = Utils.getDB();
		DBCollection entitiesColl = db.getCollection("entities");
		ArrayList<String> entityKeys = new ArrayList<>(entities.size());
		for (IBinding e : entities) {
			entityKeys.add(e.getKey());
		}
		String revisionName = revision.getName();
		entitiesColl.insert(new BasicDBObject("_id", revisionName).
				append("entities", entityKeys));
		
		DBCollection refColl = db.getCollection("references");
		BasicDBObject refsObject = buildDBObject(references, revisionName);
		refColl.insert(refsObject);
		
		DBCollection inhColl = db.getCollection("inheritances");
		inhColl.insert(buildDBObject(inheritances, revisionName));
	}

	private BasicDBObject buildDBObject(List<Dependency> dependencies,
			String revisionName) {
		BasicDBObject dbObject = new BasicDBObject("_id", revisionName);
		for (Dependency d : dependencies) {
			@SuppressWarnings("unchecked")
			ArrayList<String> targets = (ArrayList<String>) dbObject.get(d.source.getKey());
			if (targets == null) {
				targets = new ArrayList<>();
				dbObject.append(d.source.getKey(), targets);
			}
			targets.add(d.target.getKey());
		}
		return dbObject;
	}
}
