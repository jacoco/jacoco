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
package org.jacoco.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

	private ByteArrayOutputStream buffer;

	private IExecutionDataVisitor writer;

	private ExecutionDataStore store;

	private Random random;

	@Before
	public void setup() {
		buffer = new ByteArrayOutputStream();
		writer = new ExecutionDataWriter(buffer);
		store = new ExecutionDataStore();
		random = new Random(5);
	}

	@Test
	public void testEmpty() throws IOException {
		ExecutionDataReader reader = createReader();
		reader.accept(new IExecutionDataVisitor() {
			public void visitClassExecution(long id, boolean[][] blockdata) {
				fail("No data expected.");
			}
		});
	}

	@Test
	public void testMinClassId() throws IOException {
		boolean[][] blocks = createBlockdata(0, 0);
		writer.visitClassExecution(Long.MIN_VALUE, blocks);
		readIntoStore();
		assertArrayEquals(blocks, store.get(Long.MIN_VALUE));
	}

	@Test
	public void testMaxClassId() throws IOException {
		boolean[][] blocks = createBlockdata(0, 0);
		writer.visitClassExecution(Long.MAX_VALUE, blocks);
		readIntoStore();
		assertArrayEquals(blocks, store.get(Long.MAX_VALUE));
	}

	@Test
	public void testEmptyClass() throws IOException {
		boolean[][] blocks = createBlockdata(0, 0);
		writer.visitClassExecution(3, blocks);
		readIntoStore();
		assertArrayEquals(blocks, store.get(3));
	}

	@Test
	public void testEmptyMethods() throws IOException {
		boolean[][] blocks = createBlockdata(5, 0);
		writer.visitClassExecution(3, blocks);
		readIntoStore();
		assertArrayEquals(blocks, store.get(3));
	}

	@Test
	public void testOneClass() throws IOException {
		boolean[][] blocks = createBlockdata(5, 10);
		writer.visitClassExecution(3, blocks);
		readIntoStore();
		assertArrayEquals(blocks, store.get(3));
	}

	@Test
	public void testTwoClasses() throws IOException {
		boolean[][] blocks1 = createBlockdata(5, 15);
		boolean[][] blocks2 = createBlockdata(7, 12);
		writer.visitClassExecution(333, blocks1);
		writer.visitClassExecution(-45, blocks2);
		readIntoStore();
		assertArrayEquals(blocks1, store.get(333));
		assertArrayEquals(blocks2, store.get(-45));
	}

	@Test
	public void testBigClass() throws IOException {
		boolean[][] blocks = createBlockdata(43, 40);
		writer.visitClassExecution(123, blocks);
		readIntoStore();
		assertArrayEquals(blocks, store.get(123));
	}

	@Test(expected = RuntimeException.class)
	public void testIOException() throws IOException {
		ExecutionDataWriter writer = new ExecutionDataWriter(
				new OutputStream() {
					@Override
					public void write(int b) throws IOException {
						throw new IOException();
					}
				});
		boolean[][] blocks = createBlockdata(1, 1);
		writer.visitClassExecution(3, blocks);
	}

	private boolean[][] createBlockdata(int methodCount, int maxBlockCount) {
		boolean[][] blocks = new boolean[methodCount][];
		for (int i = 0; i < blocks.length; i++) {
			boolean[] arr = new boolean[random.nextInt(maxBlockCount + 1)];
			for (int j = 0; j < arr.length; j++) {
				arr[j] = random.nextBoolean();
			}
			blocks[i] = arr;
		}
		return blocks;
	}

	private void assertArrayEquals(boolean[][] expected, boolean[][] actual) {
		assertEquals(expected.length, actual.length, 0.0);
		for (int i = 0; i < expected.length; i++) {
			boolean[] b1 = expected[i];
			boolean[] b2 = actual[i];
			assertEquals(b1.length, b2.length, 0.0);
			for (int j = 0; j < b1.length; j++) {
				assertTrue(b1[j] == b2[j]);
			}
		}
	}

	private void readIntoStore() throws IOException {
		ExecutionDataReader reader = createReader();
		reader.accept(store);
	}

	private ExecutionDataReader createReader() {
		return new ExecutionDataReader(new ByteArrayInputStream(buffer
				.toByteArray()));
	}

}
