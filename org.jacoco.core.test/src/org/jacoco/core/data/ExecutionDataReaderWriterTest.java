/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ExecutionDataReader} and {@link ExecutionDataWriter}.
 * The tests don't care about the written binary format, they just verify
 * symmetry.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ExecutionDataReaderWriterTest {

	private PipedOutputStream pipe;

	private ExecutionDataWriter writer;

	private ExecutionDataReader reader;

	private ExecutionDataStore store;

	private SessionInfo sessionInfo;

	private Random random;

	@Before
	public void setup() throws IOException {
		pipe = new PipedOutputStream();
		writer = new ExecutionDataWriter(pipe);
		reader = new ExecutionDataReader(new PipedInputStream(pipe));
		store = new ExecutionDataStore();
		reader.setExecutionDataVisitor(store);
		reader.setSessionInfoVisitor(new ISessionInfoVisitor() {
			public void visitSessionInfo(SessionInfo info) {
				sessionInfo = info;
			}
		});
		random = new Random(5);
	}

	@Test
	public void testEmpty() throws IOException {
		pipe.close();
		reader.setExecutionDataVisitor(new IExecutionDataVisitor() {
			public void visitClassExecution(long id, String name,
					boolean[] blockdata) {
				fail("No data expected.");
			}
		});
		reader.read();
	}

	@Test
	public void testGetFileHeader() {
		byte[] header = ExecutionDataWriter.getFileHeader();
		assertEquals(0x01, 0xFF & header[0], 0.0);
		assertEquals(0xC0, 0xFF & header[1], 0.0);
		assertEquals(0xC0, 0xFF & header[2], 0.0);
		final char version = ExecutionDataWriter.FORMAT_VERSION;
		assertEquals(version >> 8, 0xFF & header[3], 0.0);
		assertEquals(version & 0xFF, 0xFF & header[4], 0.0);
	}

	@Test
	public void testValidHeader() throws IOException {
		writer.writeHeader();
		pipe.close();
		reader.read();
	}

	@Test(expected = IOException.class)
	public void testInvalidMagicNumber() throws IOException {
		pipe.write(ExecutionDataWriter.BLOCK_HEADER);
		pipe.write(0x12);
		pipe.write(0x34);
		pipe.close();
		reader.read();
	}

	@Test(expected = IOException.class)
	public void testInvalidHeaderVersion() throws IOException {
		pipe.write(ExecutionDataWriter.BLOCK_HEADER);
		pipe.write(0xC0);
		pipe.write(0xC0);
		final char version = ExecutionDataWriter.FORMAT_VERSION - 1;
		pipe.write(version >> 8);
		pipe.write(version & 0xFF);
		pipe.close();
		reader.read();
	}

	@Test(expected = IOException.class)
	public void testUnknownBlock() throws IOException {
		pipe.write(0xff);
		pipe.close();
		reader.read();
	}

	// === Session Info ===

	@Test(expected = IOException.class)
	public void testNoSessionInfoVisitor() throws IOException {
		pipe = new PipedOutputStream();
		writer = new ExecutionDataWriter(pipe);
		reader = new ExecutionDataReader(new PipedInputStream(pipe));
		writer.visitSessionInfo(new SessionInfo("x", 0, 1));
		pipe.close();
		reader.read();
	}

	@Test
	public void testSessionInfo() throws IOException {
		writer.visitSessionInfo(new SessionInfo("TestSession",
				2837123124567891234L, 3444234223498879234L));
		pipe.close();
		reader.read();
		assertNotNull(sessionInfo);
		assertEquals("TestSession", sessionInfo.getId());
		assertEquals(2837123124567891234L, sessionInfo.getStartTimeStamp());
		assertEquals(3444234223498879234L, sessionInfo.getDumpTimeStamp());
	}

	@Test(expected = RuntimeException.class)
	public void testSessionInfoIOException() throws IOException {
		final boolean[] broken = new boolean[1];
		final ExecutionDataWriter writer = new ExecutionDataWriter(
				new OutputStream() {
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
		pipe = new PipedOutputStream();
		writer = new ExecutionDataWriter(pipe);
		reader = new ExecutionDataReader(new PipedInputStream(pipe));
		writer.visitClassExecution(Long.MIN_VALUE, "Sample", createData(0));
		pipe.close();
		reader.read();
	}

	@Test
	public void testMinClassId() throws IOException {
		final boolean[] data = createData(0);
		writer.visitClassExecution(Long.MIN_VALUE, "Sample", data);
		pipe.close();
		reader.read();
		assertArrayEquals(data, store.getData(Long.MIN_VALUE));
	}

	@Test
	public void testMaxClassId() throws IOException {
		final boolean[] data = createData(0);
		writer.visitClassExecution(Long.MAX_VALUE, "Sample", data);
		pipe.close();
		reader.read();
		assertArrayEquals(data, store.getData(Long.MAX_VALUE));
	}

	@Test
	public void testEmptyClass() throws IOException {
		final boolean[] data = createData(0);
		writer.visitClassExecution(3, "Sample", data);
		pipe.close();
		reader.read();
		assertArrayEquals(data, store.getData(3));
	}

	@Test
	public void testOneClass() throws IOException {
		final boolean[] data = createData(5);
		writer.visitClassExecution(3, "Sample", data);
		pipe.close();
		reader.read();
		assertArrayEquals(data, store.getData(3));
	}

	@Test
	public void testTwoClasses() throws IOException {
		final boolean[] data1 = createData(5);
		final boolean[] data2 = createData(7);
		writer.visitClassExecution(333, "Sample", data1);
		writer.visitClassExecution(-45, "Sample", data2);
		pipe.close();
		reader.read();
		assertArrayEquals(data1, store.getData(333));
		assertArrayEquals(data2, store.getData(-45));
	}

	@Test
	public void testBigClass() throws IOException {
		final boolean[] data = createData(117);
		writer.visitClassExecution(123, "Sample", data);
		pipe.close();
		reader.read();
		assertArrayEquals(data, store.getData(123));
	}

	@Test(expected = RuntimeException.class)
	public void testExecutionDataIOException() throws IOException {
		final boolean[] broken = new boolean[1];
		final ExecutionDataWriter writer = new ExecutionDataWriter(
				new OutputStream() {
					@Override
					public void write(int b) throws IOException {
						if (broken[0]) {
							throw new IOException();
						}
					}
				});
		broken[0] = true;
		writer.visitClassExecution(3, "Sample", createData(1));
	}

	private boolean[] createData(final int probeCount) {
		final boolean[] data = new boolean[random.nextInt(probeCount + 1)];
		for (int j = 0; j < data.length; j++) {
			data[j] = random.nextBoolean();
		}
		return data;
	}

	private void assertArrayEquals(final boolean[] expected,
			final boolean[] actual) {
		assertEquals(expected.length, actual.length, 0.0);
		for (int i = 0; i < expected.length; i++) {
			assertTrue(expected[i] == expected[i]);
		}
	}

}
