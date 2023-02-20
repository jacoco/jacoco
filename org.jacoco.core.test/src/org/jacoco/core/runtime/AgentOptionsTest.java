/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for {@link AgentOptions}.
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
		assertEquals(AgentOptions.DEFAULT_DESTFILE, options.getDestfile());
		assertTrue(options.getAppend());
		assertEquals("*", options.getIncludes());
		assertEquals("", options.getExcludes());
		assertEquals("sun.reflect.DelegatingClassLoader",
				options.getExclClassloader());
		assertFalse(options.getInclBootstrapClasses());
		assertFalse(options.getInclNoLocationClasses());
		assertNull(options.getSessionId());
		assertTrue(options.getDumpOnExit());
		assertEquals(AgentOptions.OutputMode.file, options.getOutput());
		assertEquals(AgentOptions.DEFAULT_ADDRESS, options.getAddress());
		assertEquals(AgentOptions.DEFAULT_PORT, options.getPort());
		assertNull(options.getClassDumpDir());
		assertFalse(options.getJmx());

		assertEquals("", options.toString());
	}

	@Test
	public void testEmptyOptions() {
		AgentOptions options = new AgentOptions("");
		assertEquals("", options.toString());
	}

	@Test
	public void testNullOptions() {
		AgentOptions options = new AgentOptions((String) null);
		assertEquals("", options.toString());
	}

	@Test
	public void testPropertiesOptions() {
		Properties properties = new Properties();
		properties.put("destfile", "/target/test/test.exec");
		properties.put("append", "false");
		properties.put("includes", "org.*:com.*");
		properties.put("excludes", "*Test");
		properties.put("exclclassloader", "org.jacoco.test.TestLoader");
		properties.put("inclbootstrapclasses", "true");
		properties.put("inclnolocationclasses", "true");
		properties.put("sessionid", "testsession");
		properties.put("dumponexit", "false");
		properties.put("output", "tcpserver");
		properties.put("address", "remotehost");
		properties.put("port", "1234");
		properties.put("classdumpdir", "target/dump");
		properties.put("jmx", "true");

		AgentOptions options = new AgentOptions(properties);

		assertEquals("/target/test/test.exec", options.getDestfile());
		assertFalse(options.getAppend());
		assertEquals("org.*:com.*", options.getIncludes());
		assertEquals("*Test", options.getExcludes());
		assertEquals("org.jacoco.test.TestLoader",
				options.getExclClassloader());
		assertTrue(options.getInclBootstrapClasses());
		assertTrue(options.getInclNoLocationClasses());
		assertEquals("testsession", options.getSessionId());
		assertFalse(options.getDumpOnExit());
		assertEquals(AgentOptions.OutputMode.tcpserver, options.getOutput());
		assertEquals("remotehost", options.getAddress());
		assertEquals(1234, options.getPort());
		assertEquals("target/dump", options.getClassDumpDir());
		assertTrue(options.getJmx());
	}

	@Test
	public void testEmptyPropertiesOptions() {
		AgentOptions options = new AgentOptions(new Properties());
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
		assertEquals("org.jacoco.test.TestLoader",
				options.getExclClassloader());
	}

	@Test
	public void testSetExclClassloader() {
		AgentOptions options = new AgentOptions();
		options.setExclClassloader("org.jacoco.test.TestLoader");
		assertEquals("org.jacoco.test.TestLoader",
				options.getExclClassloader());
		assertEquals("exclclassloader=org.jacoco.test.TestLoader",
				options.toString());
	}

	@Test
	public void testGetIncludes() {
		AgentOptions options = new AgentOptions("includes=org.*:com.*");
		assertEquals("org.*:com.*", options.getIncludes());
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
	public void testGetInclBootstrapClassesTrue() {
		AgentOptions options = new AgentOptions("inclbootstrapclasses=true");
		assertTrue(options.getInclBootstrapClasses());
	}

	@Test
	public void testGetInclBootstrapClassesFalse() {
		AgentOptions options = new AgentOptions("inclbootstrapclasses=false");
		assertFalse(options.getInclBootstrapClasses());
	}

	@Test
	public void testSetInclBootstrapClassesTrue() {
		AgentOptions options = new AgentOptions();
		options.setInclBootstrapClasses(true);
		assertTrue(options.getInclBootstrapClasses());
		assertEquals("inclbootstrapclasses=true", options.toString());
	}

	@Test
	public void testSetInclBootstrapClassesFalse() {
		AgentOptions options = new AgentOptions();
		options.setInclBootstrapClasses(false);
		assertFalse(options.getInclBootstrapClasses());
		assertEquals("inclbootstrapclasses=false", options.toString());
	}

	@Test
	public void testGetInclNoLocationClassesTrue() {
		AgentOptions options = new AgentOptions("inclnolocationclasses=true");
		assertTrue(options.getInclNoLocationClasses());
	}

	@Test
	public void testGetInclNoLocationClassesFalse() {
		AgentOptions options = new AgentOptions("inclnolocationclasses=false");
		assertFalse(options.getInclNoLocationClasses());
	}

	@Test
	public void testSetInclNoLocationClassesTrue() {
		AgentOptions options = new AgentOptions();
		options.setInclNoLocationClasses(true);
		assertTrue(options.getInclNoLocationClasses());
		assertEquals("inclnolocationclasses=true", options.toString());
	}

	@Test
	public void testSetInclNoLocationClassesFalse() {
		AgentOptions options = new AgentOptions();
		options.setInclNoLocationClasses(false);
		assertFalse(options.getInclNoLocationClasses());
		assertEquals("inclnolocationclasses=false", options.toString());
	}

	@Test
	public void testGetSessionId() {
		AgentOptions options = new AgentOptions("sessionid=testsession");
		assertEquals("testsession", options.getSessionId());
	}

	@Test
	public void testSetSessionId() {
		AgentOptions options = new AgentOptions();
		options.setSessionId("testsession");
		assertEquals("testsession", options.getSessionId());
		assertEquals("sessionid=testsession", options.toString());
	}

	@Test
	public void testGetDumpOnExit() {
		AgentOptions options = new AgentOptions("dumponexit=false");
		assertFalse(options.getDumpOnExit());
	}

	@Test
	public void testSetDumpOnExit() {
		AgentOptions options = new AgentOptions();
		options.setDumpOnExit(false);
		assertFalse(options.getDumpOnExit());
	}

	@Test
	public void testGetOutput() {
		AgentOptions options = new AgentOptions("output=tcpserver");
		assertEquals(AgentOptions.OutputMode.tcpserver, options.getOutput());
	}

	@Test
	public void testSetOutput1() {
		AgentOptions options = new AgentOptions();
		options.setOutput("tcpclient");
		assertEquals(AgentOptions.OutputMode.tcpclient, options.getOutput());
	}

	@Test
	public void testSetOutput2() {
		AgentOptions options = new AgentOptions();
		options.setOutput(AgentOptions.OutputMode.tcpclient);
		assertEquals(AgentOptions.OutputMode.tcpclient, options.getOutput());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidOutput1() {
		new AgentOptions("output=foo");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidOutput2() {
		AgentOptions options = new AgentOptions();
		options.setOutput("foo");
	}

	@Test
	public void testGetPort() {
		AgentOptions options = new AgentOptions("port=1234");
		assertEquals(1234, options.getPort());
	}

	@Test
	public void testSetPort() {
		AgentOptions options = new AgentOptions();
		options.setPort(1234);
		assertEquals(1234, options.getPort());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParseInvalidPort() {
		new AgentOptions("port=xxx");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetNegativePort() {
		AgentOptions options = new AgentOptions();
		options.setPort(-1234);
	}

	@Test
	public void testGetAddress() {
		AgentOptions options = new AgentOptions("address=remotehost");
		assertEquals("remotehost", options.getAddress());
	}

	@Test
	public void testSetAddress() {
		AgentOptions options = new AgentOptions();
		options.setAddress("remotehost");
		assertEquals("remotehost", options.getAddress());
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
		new AgentOptions("destfile=test.exec,Some-thing_1=true");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidPortOptionValue() {
		new AgentOptions("port=-1234");
	}

	@Test
	public void testGetClassDumpDir() {
		AgentOptions options = new AgentOptions("classdumpdir=target/dump");
		assertEquals("target/dump", options.getClassDumpDir());
	}

	@Test
	public void testSetClassDumpDir() {
		AgentOptions options = new AgentOptions();
		options.setClassDumpDir("target/dump");
		assertEquals("target/dump", options.getClassDumpDir());
		assertEquals("classdumpdir=target/dump", options.toString());
	}

	@Test
	public void testGetJmx() {
		AgentOptions options = new AgentOptions("jmx=true");
		assertTrue(options.getJmx());
	}

	@Test
	public void testSetJmx() {
		AgentOptions options = new AgentOptions();
		options.setJmx(true);
		assertTrue(options.getJmx());
	}

	@Test
	public void testGetVMArgumentWithNoOptions() {
		AgentOptions options = new AgentOptions();
		String vmArgument = options.getVMArgument(defaultAgentJarFile);

		assertEquals(
				String.format("-javaagent:%s=", defaultAgentJarFile.toString()),
				vmArgument);
	}

	@Test
	public void testGetVMArgumentWithOneOption() {
		AgentOptions options = new AgentOptions();
		options.setAppend(true);

		String vmArgument = options.getVMArgument(defaultAgentJarFile);

		assertEquals(String.format("-javaagent:%s=append=true",
				defaultAgentJarFile.toString()), vmArgument);
	}

	@Test
	public void testGetVMArgumentWithOptions() {
		AgentOptions options = new AgentOptions();
		options.setAppend(true);
		options.setDestfile("some test.exec");
		String vmArgument = options.getVMArgument(defaultAgentJarFile);

		assertEquals(String.format(
				"-javaagent:%s=destfile=some test.exec,append=true",
				defaultAgentJarFile.toString()), vmArgument);
	}

	@Test
	public void testGetQuotedVMArgument() {
		AgentOptions options = new AgentOptions();
		options.setSessionId("my session");

		String vmArgument = options.getQuotedVMArgument(defaultAgentJarFile);

		assertEquals(String.format("\"-javaagent:%s=sessionid=my session\"",
				defaultAgentJarFile.toString()), vmArgument);
	}

	@Test
	public void testPrependVMArguments() {
		AgentOptions options = new AgentOptions();

		String vmArgument = options.prependVMArguments("a b c",
				defaultAgentJarFile);

		assertEquals(String.format("-javaagent:%s= a b c",
				defaultAgentJarFile.toString()), vmArgument);
	}

	@Test
	public void testPrependVMArgumentsReplace() {
		AgentOptions options = new AgentOptions();

		String vmArgument = options.prependVMArguments(
				String.format("a b -javaagent:%s=append=false c",
						defaultAgentJarFile),
				defaultAgentJarFile);

		assertEquals(String.format("-javaagent:%s= a b c",
				defaultAgentJarFile.toString()), vmArgument);
	}

	@Test
	public void testOptionValueWithSpecialCharacters() {
		AgentOptions options = new AgentOptions(
				"destfile=build/jacoco/foo, bar-1_0.exec");
		assertEquals("build/jacoco/foo, bar-1_0.exec", options.getDestfile());
	}

}
