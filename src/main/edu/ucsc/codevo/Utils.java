package edu.ucsc.codevo;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;

public class Utils {
	public static void log(int level, String message) {
		ResourcesPlugin plugin = ResourcesPlugin.getPlugin();
		plugin.getBundle().getSymbolicName();
		ILog logger = plugin.getLog();
		logger.log(new Status(
				level, 
				plugin.getBundle().getSymbolicName(), 
				message));
	}
}
