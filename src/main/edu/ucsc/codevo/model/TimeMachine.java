package edu.ucsc.codevo.model;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class TimeMachine {
	private Iterator<RevCommit> revisionIterator;
	private int totalRevisions;
	private Git git;
	private File gitDir;
	public TimeMachine(List<File> paths) throws IOException, GitAPIException {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		builder.findGitDir(paths.get(0));
		gitDir = builder.getGitDir();
		Repository repository = builder.build();
		git = new Git(repository);
		resetRevisionIterator(paths);
		totalRevisions = 0;
		while (revisionIterator.hasNext()) {
			revisionIterator.next();
			totalRevisions++;
		}
		resetRevisionIterator(paths);
	}
	
	private void resetRevisionIterator(List<File> paths) throws GitAPIException {
		LogCommand log = git.log();
		for (File path : paths) {
			log.addPath(getRelativePath(path));
		}
		revisionIterator = log.call().iterator();
	}
	
	String getRelativePath(File filePath) {
		// ".git".length == 4
		return filePath.getAbsolutePath().substring(
				gitDir.getAbsolutePath().length() - 4);
	}
	
	public int getTotalRevsions() {
		return totalRevisions;
	}
	
	public RevCommit next() throws GitAPIException {
		if (!revisionIterator.hasNext()) {
			return null;
		}
		RevCommit c = revisionIterator.next();
		git.checkout().setName(c.getName()).call();
		return c;
	}
}
