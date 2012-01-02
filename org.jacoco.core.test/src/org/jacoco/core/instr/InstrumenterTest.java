/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.instr;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Instrumenter}.
 */
public class InstrumenterTest {

	// no serialVersionUID to enforce calculation
	@SuppressWarnings("serial")
	public static class SerializationTarget implements Serializable {

		private final String text;

		private final int nr;

		public SerializationTarget(final String text, final int nr) {
			this.text = text;
			this.nr = nr;
		}

		@Override
		public String toString() {
			return text + nr;
		}

	}

	private SystemPropertiesRuntime runtime;

	private Instrumenter instrumenter;

	@Before
	public void setup() {
		runtime = new SystemPropertiesRuntime();
		instrumenter = new Instrumenter(runtime);
		runtime.startup();
	}

	@After
	public void teardown() {
		runtime.shutdown();
	}

	@Test
	public void testSerialization() throws Exception {
		// Create instrumented instance:
		final byte[] bytes = instrumenter.instrument(TargetLoader
				.getClassData(SerializationTarget.class));
		final TargetLoader loader = new TargetLoader(SerializationTarget.class,
				bytes);
		final Object obj1 = loader.getTargetClass()
				.getConstructor(String.class, Integer.TYPE)
				.newInstance("Hello", Integer.valueOf(42));

		// Serialize instrumented instance:
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		new ObjectOutputStream(buffer).writeObject(obj1);

		// Deserialize with original class definition:
		final Object obj2 = new ObjectInputStream(new ByteArrayInputStream(
				buffer.toByteArray())).readObject();
		assertEquals("Hello42", obj2.toString());
	}
}
