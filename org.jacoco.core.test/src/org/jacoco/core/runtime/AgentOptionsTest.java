/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and others
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

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for {@link AgentOptions}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class AgentOptionsTest {
	private static File defaultAgentJarFile;

	@BeforeClass
	public static void beforeClass() {
		defaultAgentJarFile = new File("jacocoagent.jar");
	}

	@Test
	public void testDefaults() {
		AgentOptions options = new AgentOptions();
		assertEquals("jacoco.exec", options.getDestfile());
		assertTrue(options.getAppend());
		assertEquals("*", options.getIncludes());
		assertEquals("", options.getExcludes());
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
	public void testGetDestile() {
		AgentOptions options = new AgentOptions("destfile=/var/test.exec");
		assertEquals("/var/test.exec", options.getDestfile());
	}

	@Test
	public void testSetDestile() {
		AgentOptions options = new AgentOptions();
		options.setDestfile("/var/test.exec");
		assertEquals("/var/test.exec", options.getDestfile());
		assertEquals("destfile=/var/test.exec", options.toString());
	}

	@Test
	public void testGetAppendTrue() {
		AgentOptions options = new AgentOptions("append=true");
		assertTrue(options.getAppend());
	}

	@Test
	public void testGetAppendFalse() {
		AgentOptions options = new AgentOptions("append=false");
		assertFalse(options.getAppend());
	}

	@Test
	public void testSetAppendTrue() {
		AgentOptions options = new AgentOptions();
		options.setAppend(true);
		assertTrue(options.getAppend());
		assertEquals("append=true", options.toString());
	}

	@Test
	public void testSetAppendFalse() {
		AgentOptions options = new AgentOptions();
		options.setAppend(false);
		assertFalse(options.getAppend());
		assertEquals("append=false", options.toString());
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
	public void testGetIncludes() {
		AgentOptions options = new AgentOptions("includes=org.*|com.*");
		assertEquals("org.*|com.*", options.getIncludes());
	}

	@Test
	public void testSetIncludes() {
		AgentOptions options = new AgentOptions();
		options.setIncludes("org.jacoco.*");
		assertEquals("org.jacoco.*", options.getIncludes());
		assertEquals("includes=org.jacoco.*", options.toString());
	}

	@Test
	public void testGetExcludes() {
		AgentOptions options = new AgentOptions("excludes=*Test");
		assertEquals("*Test", options.getExcludes());
	}

	@Test
	public void testSetExcludes() {
		AgentOptions options = new AgentOptions();
		options.setExcludes("org.jacoco.test.*");
		assertEquals("org.jacoco.test.*", options.getExcludes());
		assertEquals("excludes=org.jacoco.test.*", options.toString());
	}

	@Test
	public void testToString() {
		AgentOptions options = new AgentOptions();
		options.setDestfile("test.exec");
		options.setAppend(false);
		assertEquals("destfile=test.exec,append=false", options.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidOptionFormat() {
		new AgentOptions("destfile");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidOptionKey() {
		new AgentOptions("destfile=test.exec,something=true");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidOptionValue() {
		AgentOptions options = new AgentOptions();
		options.setDestfile("invalid,name.exec");
	}

	@Test
	public void testVMArgsWithNoOptions() {
		AgentOptions options = new AgentOptions();
		String vmArgument = options.getVMArgument(defaultAgentJarFile);

		assertEquals(String.format("-javaagent:%s=", defaultAgentJarFile
				.toString()), vmArgument);
	}

	@Test
	public void testVMArgsWithOneOption() {
		AgentOptions options = new AgentOptions();
		options.setAppend(true);

		String vmArgument = options.getVMArgument(defaultAgentJarFile);

		assertEquals(String.format("-javaagent:%s=append=true",
				defaultAgentJarFile.toString()), vmArgument);
	}

	@Test
	public void testVMArgsWithOptions() {
		AgentOptions options = new AgentOptions();
		options.setAppend(true);
		options.setDestfile("some test.exec");
		String vmArgument = options.getVMArgument(defaultAgentJarFile);

		assertEquals(String.format(
				"-javaagent:%s=destfile=some test.exec,append=true",
				defaultAgentJarFile.toString()), vmArgument);
	}
}
