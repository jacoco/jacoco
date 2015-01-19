/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.runtime.AbstractRuntime;
import org.jacoco.core.runtime.AgentOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.MethodVisitor;

/**
 * Unit tests for {@link CoverageTransformer}.
 */
public class CoverageTransformerTest {

	private ExceptionRecorder recorder;

	private AgentOptions options;

	private ClassLoader classLoader;

	private ProtectionDomain protectionDomain;

	private StubRuntime runtime;

	@Before
	public void setup() {
		recorder = new ExceptionRecorder();
		options = new AgentOptions();
		classLoader = getClass().getClassLoader();
		protectionDomain = getClass().getProtectionDomain();
		runtime = new StubRuntime();
	}

	@After
	public void teardown() {
		recorder.assertNoException();
	}

	@Test
	public void testHasSourceLocationNegative1() {
		CoverageTransformer t = createTransformer();
		assertFalse(t.hasSourceLocation(null));
	}

	@Test
	public void testHasSourceLocationNegative2() {
		CoverageTransformer t = createTransformer();
		ProtectionDomain pd = new ProtectionDomain(null, null);
		assertFalse(t.hasSourceLocation(pd));
	}

	@Test
	public void testHasSourceLocationNegative3() {
		CoverageTransformer t = createTransformer();
		CodeSource cs = new CodeSource(null, new Certificate[0]);
		ProtectionDomain pd = new ProtectionDomain(cs, null);
		assertFalse(t.hasSourceLocation(pd));
	}

	@Test
	public void testHasSourceLocationPositive() {
		CoverageTransformer t = createTransformer();
		assertTrue(t.hasSourceLocation(protectionDomain));
	}

	@Test
	public void testFilterAgentClass() {
		CoverageTransformer t = createTransformer();
		assertFalse(t.filter(classLoader,
				"org/jacoco/agent/rt/internal/somepkg/SomeClass"));
	}

	@Test
	public void testFilterIncludesBootstrapClassesPositive() {
		options.setInclBootstrapClasses(true);
		CoverageTransformer t = createTransformer();
		assertTrue(t.filter(null, "java/util/TreeSet"));
	}

	@Test
	public void testFilterIncludesBootstrapClassesNegative() {
		options.setInclBootstrapClasses(false);
		CoverageTransformer t = createTransformer();
		assertFalse(t.filter(null, "java/util/TreeSet"));
	}

	@Test
	public void testFilterClassLoaderPositive1() {
		options.setInclBootstrapClasses(false);
		options.setExclClassloader("org.jacoco.agent.SomeWhere$*");
		CoverageTransformer t = createTransformer();
		assertTrue(t.filter(classLoader, "org/example/Foo"));
	}

	@Test
	public void testFilterClassLoaderPositive2() {
		options.setInclBootstrapClasses(true);
		options.setExclClassloader("org.jacoco.agent.SomeWhere$*");
		CoverageTransformer t = createTransformer();
		assertTrue(t.filter(classLoader, "org/example/Foo"));
	}

	@Test
	public void testFilterClassLoaderNegative1() {
		options.setInclBootstrapClasses(false);
		options.setExclClassloader("org.jacoco.agent.rt.internal.CoverageTransformerTest$*");
		CoverageTransformer t = createTransformer();
		ClassLoader myClassLoader = new ClassLoader(null) {
		};
		assertFalse(t.filter(myClassLoader, "org/example/Foo"));
	}

	@Test
	public void testFilterClassLoaderNegative2() {
		options.setInclBootstrapClasses(true);
		options.setExclClassloader("org.jacoco.agent.rt.internal.CoverageTransformerTest$*");
		CoverageTransformer t = createTransformer();
		ClassLoader myClassLoader = new ClassLoader(null) {
		};
		assertFalse(t.filter(myClassLoader, "org/example/Foo"));
	}

	@Test
	public void testFilterIncludedClassPositive() {
		options.setIncludes("org.jacoco.core.*:org.jacoco.agent.rt.*");
		CoverageTransformer t = createTransformer();
		assertTrue(t.filter(classLoader, "org/jacoco/core/Foo"));
	}

	@Test
	public void testFilterIncludedClassNegative() {
		options.setIncludes("org.jacoco.core.*:org.jacoco.agent.rt.*");
		CoverageTransformer t = createTransformer();
		assertFalse(t.filter(classLoader, "org/jacoco/report/Foo"));
	}

	@Test
	public void testFilterExcludedClassPositive() {
		options.setExcludes("*Test");
		CoverageTransformer t = createTransformer();
		assertFalse(t.filter(classLoader, "org/jacoco/core/FooTest"));
	}

	@Test
	public void testFilterExcludedClassPositiveInner() {
		options.setExcludes("org.jacoco.example.Foo$Inner");
		CoverageTransformer t = createTransformer();
		assertFalse(t.filter(classLoader, "org/jacoco/example/Foo$Inner"));
	}

	@Test
	public void testFilterExcludedClassNegative() {
		options.setExcludes("*Test");
		CoverageTransformer t = createTransformer();
		assertTrue(t.filter(classLoader, "org/jacoco/core/Foo"));
	}

	@Test
	public void testTransformFiltered1() throws IllegalClassFormatException {
		CoverageTransformer t = createTransformer();
		assertNull(t.transform(classLoader, "org.jacoco.Sample", null, null,
				new byte[0]));
	}

	@Test
	public void testTransformFiltered2() throws IllegalClassFormatException {
		CoverageTransformer t = createTransformer();
		assertNull(t.transform(null, "org.jacoco.Sample", null,
				protectionDomain, new byte[0]));
	}

	@Test
	public void testTransformFailure() {
		CoverageTransformer t = createTransformer();
		try {
			t.transform(classLoader, "org.jacoco.Sample", null,
					protectionDomain, null);
			fail("IllegalClassFormatException expected.");
		} catch (IllegalClassFormatException e) {
			assertEquals("Error while instrumenting class org.jacoco.Sample.",
					e.getMessage());
		}
		recorder.assertException(IllegalClassFormatException.class,
				"Error while instrumenting class org.jacoco.Sample.",
				IOException.class);
		recorder.clear();
	}

	@Test
	public void testRedefinedClass() throws Exception {
		CoverageTransformer t = createTransformer();
		// Just pick any non-system class outside our namespace
		final Class<?> target = JaCoCo.class;
		assertNull(t.transform(classLoader, target.getName(), target,
				protectionDomain, getClassData(target)));
	}

	private CoverageTransformer createTransformer() {
		return new CoverageTransformer(runtime, options, recorder);
	}

	private static byte[] getClassData(Class<?> clazz) throws IOException {
		final String resource = "/" + clazz.getName().replace('.', '/')
				+ ".class";
		final InputStream in = clazz.getResourceAsStream(resource);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[0x100];
		int len;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		in.close();
		return out.toByteArray();
	}

	private static class StubRuntime extends AbstractRuntime {

		public StubRuntime() {
		}

		public int generateDataAccessor(long classid, String classname,
				int probecount, MethodVisitor mv) {
			return 0;
		}

		public void shutdown() {
		}

	}

}
