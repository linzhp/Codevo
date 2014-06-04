package edu.ucsc.codevo.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class DependencyTest {

	@Test
	public void testToString() {
		Dependency d = new Dependency("source", "target");
		assertEquals("source->target", d.toString());
	}

}
