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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataReaderWriterTest;
import org.jacoco.core.data.ExecutionDataWriter;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ExecutionDataReader} and {@link ExecutionDataWriter}.
 * The tests don't care about the written binary format, they just verify
 * symmetry.
 */
public class RemoteControlReaderWriterTest
		extends ExecutionDataReaderWriterTest {

	private RemoteControlWriter writer;

	@Before
	@Override
	public void setup() throws IOException {
		super.setup();
		writer = createWriter(buffer);
	}

	@Test(expected = IOException.class)
	public void testNoRemoteCommandVisitor() throws IOException {
		writer.visitDumpCommand(false, false);
		final RemoteControlReader reader = createReader();
		reader.read();
	}

	@Test
	public void testVisitDump1() throws IOException {
		testVisitDump(false, false);
	}

	@Test
	public void testVisitDump2() throws IOException {
		testVisitDump(false, true);
	}

	@Test
	public void testVisitDump3() throws IOException {
		testVisitDump(true, false);
	}

	@Test
	public void testVisitDump4() throws IOException {
		testVisitDump(true, true);
	}

	private void testVisitDump(boolean doDump, boolean doReset)
			throws IOException {
		writer.visitDumpCommand(doDump, doReset);
		final RemoteControlReader reader = createReader();
		final StringBuilder calls = new StringBuilder();
		reader.setRemoteCommandVisitor(new IRemoteCommandVisitor() {

			public void visitDumpCommand(boolean dump, boolean reset) {
				calls.append("cmd(" + dump + "," + reset + ")");
			}
		});
		assertFalse(reader.read());
		assertEquals("cmd(" + doDump + "," + doReset + ")", calls.toString());
	}

	@Test
	public void testSendCmdOk() throws IOException {
		writer.sendCmdOk();
		final RemoteControlReader reader = createReader();
		assertTrue(reader.read());
	}

	@Override
	protected RemoteControlReader createReader() throws IOException {
		return new RemoteControlReader(
				new ByteArrayInputStream(buffer.toByteArray()));
	}

	@Override
	protected RemoteControlWriter createWriter(OutputStream out)
			throws IOException {
		return new RemoteControlWriter(out);
	}

}
