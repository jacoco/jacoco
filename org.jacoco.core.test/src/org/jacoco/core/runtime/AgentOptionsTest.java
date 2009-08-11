/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link AgentOptions}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class AgentOptionsTest {

	@Test
	public void testDefaults() {
		AgentOptions options = new AgentOptions();
		assertEquals("jacoco.exec", options.getFile());
		assertTrue(options.getMerge());
		assertEquals("sun.reflect.DelegatingClassLoader", options
				.getExclClassloader());
		assertEquals("", options.toString());
	}

	@Test
	public void testEmptyOptions() {
		AgentOptions options = new AgentOptions("");
		assertEquals("", options.toString());
	}

	@Test
	public void testGetFile() {
		AgentOptions options = new AgentOptions("file=/var/test.exec");
		assertEquals("/var/test.exec", options.getFile());
	}

	@Test
	public void testSetFile() {
		AgentOptions options = new AgentOptions();
		options.setFile("/var/test.exec");
		assertEquals("/var/test.exec", options.getFile());
		assertEquals("file=/var/test.exec", options.toString());
	}

	@Test
	public void testGetMergeTrue() {
		AgentOptions options = new AgentOptions("merge=true");
		assertTrue(options.getMerge());
	}

	@Test
	public void testGetMergeFalse() {
		AgentOptions options = new AgentOptions("merge=false");
		assertFalse(options.getMerge());
	}

	@Test
	public void testSetMergeTrue() {
		AgentOptions options = new AgentOptions();
		options.setMerge(true);
		assertTrue(options.getMerge());
		assertEquals("merge=true", options.toString());
	}

	@Test
	public void testSetMergeFalse() {
		AgentOptions options = new AgentOptions();
		options.setMerge(false);
		assertFalse(options.getMerge());
		assertEquals("merge=false", options.toString());
	}

	@Test
	public void testGetExclClassloader() {
		AgentOptions options = new AgentOptions(
				"exclclassloader=org.jacoco.test.TestLoader");
		assertEquals("org.jacoco.test.TestLoader", options.getExclClassloader());
	}

	@Test
	public void testSetExclClassloader() {
		AgentOptions options = new AgentOptions();
		options.setExclClassloader("org.jacoco.test.TestLoader");
		assertEquals("org.jacoco.test.TestLoader", options.getExclClassloader());
		assertEquals("exclclassloader=org.jacoco.test.TestLoader", options
				.toString());
	}

	@Test
	public void testToString() {
		AgentOptions options = new AgentOptions();
		options.setFile("test.exec");
		options.setMerge(false);
		assertEquals("file=test.exec,merge=false", options.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidOptionFormat() {
		new AgentOptions("file");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidOptionKey() {
		new AgentOptions("file=test.exec,something=true");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidOptionValue() {
		AgentOptions options = new AgentOptions();
		options.setFile("invalid,name.exec");
	}

}
