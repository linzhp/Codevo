package edu.ucsc.codevo;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

public class Utils {
	public static void log(int level, String message) {
		Plugin plugin = Activator.getDefault();
		ILog logger = plugin.getLog();
		logger.log(new Status(
				level,
				plugin.getBundle().getSymbolicName(),
				message));
	}

	public static void main(String[] args) throws Exception {
	}
}
