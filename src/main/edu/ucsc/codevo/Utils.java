package edu.ucsc.codevo;

import java.net.UnknownHostException;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class Utils {
	public static void log(int level, String message) {
		Plugin plugin = Activator.getDefault();
		ILog logger = plugin.getLog();
		logger.log(new Status(
				level,
				plugin.getBundle().getSymbolicName(),
				message));
	}

	private static DB db;
	
	public static DB getDB() throws UnknownHostException {
		if (db == null) {
			MongoClient client = new MongoClient();
			db = client.getDB("evolution");
		}
		return db;
	}
	
	public static void main(String[] args) throws Exception {
	}
}
