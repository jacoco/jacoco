/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.instrument.IllegalClassFormatException;

import org.jacoco.core.runtime.AgentOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CoverageTransformer}.
 */
public class CoverageTransformerTest {

	private ExceptionRecorder recorder;

	private AgentOptions options;

	private ClassLoader classLoader;

	@Before
	public void setup() {
		recorder = new ExceptionRecorder();
		options = new AgentOptions();
		classLoader = getClass().getClassLoader();
	}

	@After
	public void teardown() {
		recorder.assertEmpty();
	}

	@Test
	public void testFilterAgentClass() {
		CoverageTransformer t = createTransformer();
		assertFalse(t.filter(classLoader,
				"org/jacoco/agent/rt/somepkg/SomeClass"));
	}

	@Test
	public void testFilterSystemClass() {
		CoverageTransformer t = createTransformer();
		assertFalse(t.filter(null, "org/example/Foo"));
	}

	@Test
	public void testFilterClassLoaderPositive() {
		options.setExclClassloader("org.jacoco.agent.SomeWhere$*");
		CoverageTransformer t = createTransformer();
		assertTrue(t.filter(classLoader, "org/example/Foo"));
	}

	@Test
	public void testFilterClassLoaderNegative() {
		options.setExclClassloader("org.jacoco.agent.rt.CoverageTransformerTest$*");
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
	public void testFilterExcludedClassNegative() {
		options.setExcludes("*Test");
		CoverageTransformer t = createTransformer();
		assertTrue(t.filter(classLoader, "org/jacoco/core/Foo"));
	}

	@Test
	public void testTransformFiltered() throws IllegalClassFormatException {
		CoverageTransformer t = createTransformer();
		assertNull(t.transform(null, "org.jacoco.Sample", null, null,
				new byte[0]));
	}

	@Test
	public void testTransformFailure() {
		CoverageTransformer t = createTransformer();
		try {
			t.transform(classLoader, "org.jacoco.Sample", null, null,
					new byte[0]);
			fail("IllegalClassFormatException expected.");
		} catch (IllegalClassFormatException e) {
			assertEquals("Error while instrumenting class org.jacoco.Sample.",
					e.getMessage());
		}
		recorder.assertException(IllegalClassFormatException.class,
				"Error while instrumenting class org.jacoco.Sample.");
		recorder.clear();
	}

	private CoverageTransformer createTransformer() {
		return new CoverageTransformer(new StubRuntime(), options, recorder);
	}

}
