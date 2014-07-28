package edu.ucsc.codevo.model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.jgit.api.errors.GitAPIException;
import static org.hamcrest.Matchers.*;
import org.junit.Test;

public class TimeMachineTest {

	@Test
	public void testRelativePath() throws IOException, GitAPIException {
		File file = new File("./src/test/");
		TimeMachine timeMachine = new TimeMachine(Arrays.asList(file));
		assertEquals("src/test", timeMachine.getRelativePath(file));
	}

	@Test
	public void testTotalRevisions() throws IOException, GitAPIException {
		File file = new File("./src/test/");
		TimeMachine timeMachine = new TimeMachine(Arrays.asList(file));
		assertThat(timeMachine.getTotalRevsions(), greaterThan(0));
	}
}
