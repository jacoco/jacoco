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

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CompactDataInput} and {@link CompactDataOutput}. The
 * tests don't care about the written binary format, they just verify symmetry.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CompactDataInputOutputTest {

	private CompactDataOutput out;

	private CompactDataInput in;

	@Before
	public void setup() throws IOException {
		PipedOutputStream pipe = new PipedOutputStream();
		out = new CompactDataOutput(pipe);
		in = new CompactDataInput(new PipedInputStream(pipe));
	}

	@Test
	public void testVarInt0x00000000() throws IOException {
		testVarInt(0x00000000);
	}

	@Test
	public void testVarInt0x0000007F() throws IOException {
		testVarInt(0x0000007F);
	}

	@Test
	public void testVarInt0x00000080() throws IOException {
		testVarInt(0x00000080);
	}

	@Test
	public void testVarInt0x00000100() throws IOException {
		testVarInt(0x00000100);
	}

	@Test
	public void testVarInt0x12345678() throws IOException {
		testVarInt(0x12345678);
	}

	@Test
	public void testVarIntMinus1() throws IOException {
		testVarInt(-1);
	}

	@Test
	public void testVarIntMinValue() throws IOException {
		testVarInt(Integer.MIN_VALUE);
	}

	@Test
	public void testVarIntMaxValue() throws IOException {
		testVarInt(Integer.MAX_VALUE);
	}

	private void testVarInt(int value) throws IOException {
		out.writeVarInt(value);
		out.close();
		assertEquals(Long.valueOf(value), Long.valueOf(in.readVarInt()));
		assertEquals(Integer.valueOf(-1), Integer.valueOf(in.read()));
	}

}
