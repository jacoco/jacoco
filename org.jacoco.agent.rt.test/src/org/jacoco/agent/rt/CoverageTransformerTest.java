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
package org.jacoco.agent.rt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jacoco.core.runtime.AbstractRuntime;
import org.jacoco.core.runtime.AgentOptions;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.MethodVisitor;

/**
 * Unit tests for {@link CoverageTransformer}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CoverageTransformerTest {

	private AgentOptions options;

	@Before
	public void setup() {
		options = new AgentOptions();
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
		assertTrue(t.filter(new ClassLoader(null) {
		}, "org/example/Foo"));
	}

	@Test
	public void testFilterClassLoaderNegative() {
		options
				.setExclClassloader("org.jacoco.agent.rt.CoverageTransformerTest$*");
		CoverageTransformer t = createTransformer();
		assertFalse(t.filter(new ClassLoader(null) {
		}, "org/example/Foo"));
	}

	@Test
	public void testFilterIncludedClassPositive() {
		options.setIncludes("org.jacoco.core.*|org.jacoco.agent.rt.*");
		CoverageTransformer t = createTransformer();
		assertTrue(t.filter(new ClassLoader(null) {
		}, "org/jacoco/core/Foo"));
	}

	@Test
	public void testFilterIncludedClassNegative() {
		options.setIncludes("org.jacoco.core.*|org.jacoco.agent.rt.*");
		CoverageTransformer t = createTransformer();
		assertFalse(t.filter(new ClassLoader(null) {
		}, "org/jacoco/report/Foo"));
	}

	@Test
	public void testFilterExcludedClassPositive() {
		options.setExcludes("*Test");
		CoverageTransformer t = createTransformer();
		assertFalse(t.filter(new ClassLoader(null) {
		}, "org/jacoco/core/FooTest"));
	}

	@Test
	public void testFilterExcludedClassNegative() {
		options.setExcludes("*Test");
		CoverageTransformer t = createTransformer();
		assertTrue(t.filter(new ClassLoader(null) {
		}, "org/jacoco/core/Foo"));
	}

	private CoverageTransformer createTransformer() {
		return new CoverageTransformer(new StubRuntime(), options);
	}

	private static class StubRuntime extends AbstractRuntime {
		public int generateDataAccessor(long classid, MethodVisitor mv) {
			return 0;
		}

		public void startup() {
		}

		public void shutdown() {
		}
	}

}
