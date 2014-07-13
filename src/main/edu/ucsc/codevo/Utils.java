package edu.ucsc.codevo;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

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

	public static void main(String[] args) throws Exception {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository = builder.
				findGitDir(new File("E:\\Data\\voldemort")).build();
		Git git = new Git(repository);
		Iterable<RevCommit> commits = git.log().addPath("src/java").call();
		for (RevCommit c : commits) {
			//			git.checkout().setName(c.getName()).call();
			System.out.println(c.getName() + ": " + c.getShortMessage());
		}

	}
}
