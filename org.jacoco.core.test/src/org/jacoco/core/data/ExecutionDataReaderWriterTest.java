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
package org.jacoco.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ExecutionDataReader} and {@link ExecutionDataWriter}.
 * The tests don't care about the written binary format, they just verify
 * symmetry.
 */
public class ExecutionDataReaderWriterTest {

	protected ByteArrayOutputStream buffer;

	private ExecutionDataWriter writer;

	private ExecutionDataStore store;

	private SessionInfo sessionInfo;

	private Random random;

	@Before
	public void setup() throws IOException {
		buffer = new ByteArrayOutputStream();
		writer = createWriter(buffer);
		store = new ExecutionDataStore();
		random = new Random(5);
	}

	@Test
	public void testEmpty() throws IOException {
		final ExecutionDataReader reader = createReader();
		reader.setSessionInfoVisitor(new ISessionInfoVisitor() {
			public void visitSessionInfo(final SessionInfo info) {
				fail("No data expected.");
			}
		});
		reader.setExecutionDataVisitor(new IExecutionDataVisitor() {
			public void visitClassExecution(final ExecutionData data) {
				fail("No data expected.");
			}
		});
		assertFalse(reader.read());
	}

	@Test
	public void testFlush() throws IOException {
		final boolean[] flushCalled = new boolean[] { false };
		final OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			}

			@Override
			public void flush() throws IOException {
				flushCalled[0] = true;
			}
		};
		new ExecutionDataWriter(out).flush();
		assertTrue(flushCalled[0]);
	}

	@Test
	public void testCustomBlocks() throws IOException {
		buffer.write(0x55);
		buffer.write(0x66);
		final ExecutionDataReader reader = new ExecutionDataReader(
				new ByteArrayInputStream(buffer.toByteArray())) {

			@Override
			protected boolean readBlock(byte blocktype) throws IOException {
				switch (blocktype) {
				case 0x55:
					return true;
				case 0x66:
					return false;
				}
				return super.readBlock(blocktype);
			}
		};
		assertTrue(reader.read());
	}

	@Test
	public void testGetFileHeader() {
		byte[] header = ExecutionDataWriter.getFileHeader();
		assertEquals(5, header.length);
		assertEquals(0x01, 0xFF & header[0]);
		assertEquals(0xC0, 0xFF & header[1]);
		assertEquals(0xC0, 0xFF & header[2]);
		final char version = ExecutionDataWriter.FORMAT_VERSION;
		assertEquals(version >> 8, 0xFF & header[3]);
		assertEquals(version & 0xFF, 0xFF & header[4]);
	}

	@Test
	public void testMultipleHeaders() throws IOException {
		new ExecutionDataWriter(buffer);
		new ExecutionDataWriter(buffer);
		new ExecutionDataWriter(buffer);
		assertFalse(createReader().read());
	}

	@Test(expected = IOException.class)
	public void testInvalidMagicNumber() throws IOException {
		buffer = new ByteArrayOutputStream();
		buffer.write(ExecutionDataWriter.BLOCK_HEADER);
		buffer.write(0x12);
		buffer.write(0x34);
		createReader().read();
	}

	@Test(expected = IncompatibleExecDataVersionException.class)
	public void testInvalidVersion() throws IOException {
		buffer = new ByteArrayOutputStream();
		buffer.write(ExecutionDataWriter.BLOCK_HEADER);
		buffer.write(0xC0);
		buffer.write(0xC0);
		final char version = (char) (ExecutionDataWriter.FORMAT_VERSION - 1);
		buffer.write(version >> 8);
		buffer.write(version & 0xFF);
		createReader().read();
	}

	@Test(expected = IOException.class)
	public void testMissingHeader() throws IOException {
		buffer.reset();
		writer.visitClassExecution(
				new ExecutionData(Long.MIN_VALUE, "Sample", createData(8)));
		createReaderWithVisitors().read();
	}

	@Test(expected = IOException.class)
	public void testUnknownBlock() throws IOException {
		buffer.write(0xff);
		createReader().read();
	}

	@Test(expected = EOFException.class)
	public void testTruncatedFile() throws IOException {
		writer.visitClassExecution(
				new ExecutionData(Long.MIN_VALUE, "Sample", createData(8)));
		final byte[] content = buffer.toByteArray();
		buffer.reset();
		buffer.write(content, 0, content.length - 1);
		createReaderWithVisitors().read();
	}

	@Test
	public void testEmptyFile() throws IOException {
		buffer.reset();
		createReader().read();
	}

	// === Session Info ===

	@Test(expected = IOException.class)
	public void testNoSessionInfoVisitor() throws IOException {
		writer.visitSessionInfo(new SessionInfo("x", 0, 1));
		createReader().read();
	}

	@Test
	public void testSessionInfo() throws IOException {
		writer.visitSessionInfo(new SessionInfo("TestSession",
				2837123124567891234L, 3444234223498879234L));
		assertFalse(createReaderWithVisitors().read());
		assertNotNull(sessionInfo);
		assertEquals("TestSession", sessionInfo.getId());
		assertEquals(2837123124567891234L, sessionInfo.getStartTimeStamp());
		assertEquals(3444234223498879234L, sessionInfo.getDumpTimeStamp());
	}

	@Test(expected = RuntimeException.class)
	public void testSessionInfoIOException() throws IOException {
		final boolean[] broken = new boolean[1];
		final ExecutionDataWriter writer = createWriter(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				if (broken[0]) {
					throw new IOException();
				}
			}
		});
		broken[0] = true;
		writer.visitSessionInfo(new SessionInfo("X", 0, 0));
	}

	// === Execution Data ===

	@Test(expected = IOException.class)
	public void testNoExecutionDataVisitor() throws IOException {
		writer.visitClassExecution(
				new ExecutionData(Long.MIN_VALUE, "Sample", createData(8)));
		createReader().read();
	}

	@Test
	public void testMinClassId() throws IOException {
		final boolean[] data = createData(8);
		writer.visitClassExecution(
				new ExecutionData(Long.MIN_VALUE, "Sample", data));
		assertFalse(createReaderWithVisitors().read());
		assertArrayEquals(data, store.get(Long.MIN_VALUE).getProbes());
	}

	@Test
	public void testMaxClassId() throws IOException {
		final boolean[] data = createData(8);
		writer.visitClassExecution(
				new ExecutionData(Long.MAX_VALUE, "Sample", data));
		assertFalse(createReaderWithVisitors().read());
		assertArrayEquals(data, store.get(Long.MAX_VALUE).getProbes());
	}

	@Test
	public void testEmptyClass() throws IOException {
		final boolean[] data = createData(0);
		writer.visitClassExecution(new ExecutionData(3, "Sample", data));
		assertFalse(createReaderWithVisitors().read());
		assertTrue(store.getContents().isEmpty());
	}

	@Test
	public void testNoHitClass() throws IOException {
		final boolean[] data = new boolean[] { false, false, false };
		writer.visitClassExecution(new ExecutionData(3, "Sample", data));
		assertFalse(createReaderWithVisitors().read());
		assertTrue(store.getContents().isEmpty());
	}

	@Test
	public void testOneClass() throws IOException {
		final boolean[] data = createData(15);
		writer.visitClassExecution(new ExecutionData(3, "Sample", data));
		assertFalse(createReaderWithVisitors().read());
		assertArrayEquals(data, store.get(3).getProbes());
	}

	@Test
	public void testTwoClasses() throws IOException {
		final boolean[] data1 = createData(15);
		final boolean[] data2 = createData(185);
		writer.visitClassExecution(new ExecutionData(333, "Sample", data1));
		writer.visitClassExecution(new ExecutionData(-45, "Sample", data2));
		assertFalse(createReaderWithVisitors().read());
		assertArrayEquals(data1, store.get(333).getProbes());
		assertArrayEquals(data2, store.get(-45).getProbes());
	}

	@Test
	public void testBigClass() throws IOException {
		final boolean[] data = createData(3599);
		writer.visitClassExecution(new ExecutionData(123, "Sample", data));
		assertFalse(createReaderWithVisitors().read());
		assertArrayEquals(data, store.get(123).getProbes());
	}

	@Test(expected = RuntimeException.class)
	public void testExecutionDataIOException() throws IOException {
		final boolean[] broken = new boolean[1];
		final ExecutionDataWriter writer = createWriter(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				if (broken[0]) {
					throw new IOException();
				}
			}
		});
		broken[0] = true;
		writer.visitClassExecution(
				new ExecutionData(3, "Sample", createData(1)));
	}

	private ExecutionDataReader createReaderWithVisitors() throws IOException {
		final ExecutionDataReader reader = createReader();
		reader.setExecutionDataVisitor(store);
		reader.setSessionInfoVisitor(new ISessionInfoVisitor() {
			public void visitSessionInfo(SessionInfo info) {
				sessionInfo = info;
			}
		});
		return reader;
	}

	private boolean[] createData(final int probeCount) {
		final boolean[] data = new boolean[probeCount];
		for (int j = 0; j < data.length; j++) {
			data[j] = random.nextBoolean();
		}
		return data;
	}

	private void assertArrayEquals(final boolean[] expected,
			final boolean[] actual) {
		assertTrue(Arrays.equals(expected, actual));
	}

	protected ExecutionDataWriter createWriter(OutputStream out)
			throws IOException {
		return new ExecutionDataWriter(out);
	}

	protected ExecutionDataReader createReader() throws IOException {
		return new ExecutionDataReader(
				new ByteArrayInputStream(buffer.toByteArray()));
	}

}
