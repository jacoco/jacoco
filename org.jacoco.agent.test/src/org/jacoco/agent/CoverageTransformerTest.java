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
package org.jacoco.agent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jacoco.core.runtime.AbstractRuntime;
import org.jacoco.core.runtime.AgentOptions;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.commons.GeneratorAdapter;

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
		assertFalse(t.filter(null));
	}

	@Test
	public void testFilterClassLoaderPositive() {
		options.setExclClassloader("org.jacoco.agent.SomeWhere$*");
		CoverageTransformer t = createTransformer();
		assertTrue(t.filter(new ClassLoader(null) {
		}));
	}

	@Test
	public void testFilterClassLoaderNegative() {
		options
				.setExclClassloader("org.jacoco.agent.CoverageTransformerTest$*");
		CoverageTransformer t = createTransformer();
		assertFalse(t.filter(new ClassLoader(null) {
		}));
	}

	private CoverageTransformer createTransformer() {
		return new CoverageTransformer(new StubRuntime(), options);
	}

	private static class StubRuntime extends AbstractRuntime {
		public void generateDataAccessor(long classid, GeneratorAdapter gen) {
		}

		public void startup() {
		}

		public void shutdown() {
		}
	}

}
