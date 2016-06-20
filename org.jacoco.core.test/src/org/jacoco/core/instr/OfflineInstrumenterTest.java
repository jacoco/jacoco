/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jacoco.core.runtime.OfflineInstrumentationCompanionAccessGenerator;
import org.jacoco.core.test.TargetLoader;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Instrumenter} with
 * {@link OfflineInstrumentationCompanionAccessGenerator}.
 */
public class OfflineInstrumenterTest {

	private Instrumenter instrumenter;

	@Before
	public void setup() {
		instrumenter = new Instrumenter(
				new OfflineInstrumentationCompanionAccessGenerator());
	}

	@Test
	public void testInstrumentAll_Zip() throws IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final ZipOutputStream zipout = new ZipOutputStream(buffer);
		zipout.putNextEntry(new ZipEntry("Test.class"));
		zipout.write(TargetLoader.getClassDataAsBytes(getClass()));
		zipout.finish();
		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		final int count = instrumenter.instrumentAll(
				new ByteArrayInputStream(buffer.toByteArray()), out, "Test");

		assertEquals(1, count);
		final ZipInputStream zipin = new ZipInputStream(
				new ByteArrayInputStream(out.toByteArray()));
		assertEquals("Test.class", zipin.getNextEntry().getName());
		assertNotNull(zipin.getNextEntry());
		assertNull(zipin.getNextEntry());
	}

	@Test
	public void testInstrumentClass() throws IOException {
		final byte[] bytes = instrumenter.instrument(
				TargetLoader.getClassDataAsBytes(OfflineInstrumenterTest.class),
				"Test");
		try {
			instrumenter.instrument(bytes, "Test");
			fail("IOException expected");
		} catch (IOException e) {
			assertEquals("Error while instrumenting class Test.",
					e.getMessage());
			assertEquals("Already instrumented.", e.getCause().getMessage());
		}
	}

	/**
	 * Stricter than without "companion" classes - see
	 * {@link InstrumenterTest#testInstrumentInterface()}.
	 */
	@Test
	public void testInstrumentInterface() throws IOException {
		final byte[] bytes = instrumenter
				.instrument(
						TargetLoader.getClassDataAsBytes(
								InstrumenterTest.InterfaceTarget.class),
				"Test");
		try {
			instrumenter.instrument(bytes, "Test");
			fail("IOException expected");
		} catch (IOException e) {
			assertEquals("Error while instrumenting class Test.",
					e.getMessage());
			assertEquals("Already instrumented.", e.getCause().getMessage());
		}
	}

}
