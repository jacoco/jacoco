/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.ProbeMode;
import org.jacoco.core.internal.data.CompactDataInput;
import org.junit.Test;

public class ProbeArrayServiceTest {

	@Test
	public void configDefault() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(null);
		assertEquals(ProbeMode.exists, ProbeArrayService.getProbeMode());
		assertEquals(ProbeMode.exists, ProbeArrayService.newProbeArray(3)
				.getProbeMode());
	}

	@Test
	public void configGoodCount() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.count);
		assertEquals(ProbeMode.count, ProbeArrayService.getProbeMode());
		assertEquals(ProbeMode.count, ProbeArrayService.newProbeArray(3)
				.getProbeMode());
	}

	@Test
	public void configGoodParallel() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.parallelcount);
		assertEquals(ProbeMode.parallelcount, ProbeArrayService.getProbeMode());
		assertEquals(ProbeMode.parallelcount, ProbeArrayService
				.newProbeArray(3).getProbeMode());
	}

	@Test
	public void configDup() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.parallelcount);
		ProbeArrayService.configure(ProbeMode.parallelcount);
		assertEquals(ProbeMode.parallelcount, ProbeArrayService.getProbeMode());
	}

	@Test
	public void configNull() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.parallelcount);
		ProbeArrayService.configure(null);
		ProbeArrayService.configure(null);
		assertEquals(ProbeMode.parallelcount, ProbeArrayService.getProbeMode());
	}

	@Test(expected = IllegalStateException.class)
	public void configBad() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.parallelcount);
		ProbeArrayService.configure(ProbeMode.exists);
	}

	@Test
	public void newInstance_exists() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.exists);
		IProbeArray<?> result = ProbeArrayService.newProbeArray(3);

		assertTrue(result instanceof ProbeBooleanArray);
		assertEquals(3, result.length());
	}

	@Test
	public void newInstance_count() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.count);
		IProbeArray<?> result = ProbeArrayService.newProbeArray(3);

		assertTrue(result instanceof ProbeIntArray);
		assertEquals(3, result.length());
	}

	@Test
	public void newInstance_parallelcount() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.parallelcount);
		IProbeArray<?> result = ProbeArrayService.newProbeArray(3);

		assertTrue(result instanceof ProbeDoubleIntArray);
		assertEquals(3, result.length());
	}

	@Test
	public void newInstance_existsObject() {
		IProbeArray<?> result = ProbeArrayService.newProbeArray(new boolean[3]);

		assertTrue(result instanceof ProbeBooleanArray);
		assertEquals(3, result.length());
	}

	@Test
	public void newInstance_countObject() {
		IProbeArray<?> result = ProbeArrayService
				.newProbeArray(new AtomicIntegerArray(3));

		assertTrue(result instanceof ProbeIntArray);
		assertEquals(3, result.length());
	}

	@Test
	public void newInstance_parallelcountObject() {
		IProbeArray<?> result = ProbeArrayService
				.newProbeArray(new ProbeDoubleIntArray(3));

		assertTrue(result instanceof ProbeDoubleIntArray);
		assertEquals(3, result.length());
	}

	@Test(expected = IllegalArgumentException.class)
	public void newInstance_badObject() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.parallelcount);

		ProbeArrayService.newProbeArray(new Object[3]);
	}

	@Test(expected = EOFException.class)
	public void read_nodata() throws Exception {
		CompactDataInput cdi = new CompactDataInput(new ByteArrayInputStream(
				new byte[] {}));
		ProbeArrayService.read(ExecutionDataWriter.FORMAT_VERSION, cdi);
	}

	@Test
	public void read_emptyExists() throws Exception {
		CompactDataInput cdi = new CompactDataInput(new ByteArrayInputStream(
				new byte[] { 00 }));

		IProbeArray<?> result = ProbeArrayService.read(
				ExecutionDataWriter.FORMAT_VERSION, cdi);

		assertTrue(result instanceof ProbeBooleanArray);
		assertEquals(0, result.length());
	}

	@Test
	public void read_oneExists() throws Exception {
		CompactDataInput cdi = new CompactDataInput(new ByteArrayInputStream(
				new byte[] { 01, 01 }));

		IProbeArray<?> result = ProbeArrayService.read(
				ExecutionDataWriter.FORMAT_VERSION, cdi);

		assertTrue(result instanceof ProbeBooleanArray);
		assertEquals(1, result.length());
		assertTrue(result.isProbeCovered(0));
	}

	@Test
	public void read_emptyCount() throws Exception {
		CompactDataInput cdi = new CompactDataInput(new ByteArrayInputStream(
				new byte[] { 00 }));

		IProbeArray<?> result = ProbeArrayService.read(
				ExecutionDataWriter.FORMAT_INT_VERSION, cdi);

		assertTrue(result instanceof ProbeIntArray);
		assertEquals(0, result.length());
	}

	@Test
	public void read_oneCount() throws Exception {
		CompactDataInput cdi = new CompactDataInput(new ByteArrayInputStream(
				new byte[] { 01, 03 }));

		IProbeArray<?> result = ProbeArrayService.read(
				ExecutionDataWriter.FORMAT_INT_VERSION, cdi);

		assertTrue(result instanceof ProbeIntArray);
		assertEquals(1, result.length());
		assertTrue(result.isProbeCovered(0));
		assertEquals(3, result.getExecutionProbe(0));
	}

	@Test
	public void read_emptyParallel() throws Exception {
		CompactDataInput cdi = new CompactDataInput(new ByteArrayInputStream(
				new byte[] { 00, 00 }));

		IProbeArray<?> result = ProbeArrayService.read(
				ExecutionDataWriter.FORMAT_DOUBLEINT_VERSION, cdi);

		assertTrue(result instanceof ProbeDoubleIntArray);
		assertEquals(0, result.length());
	}

	@Test
	public void read_oneParallel() throws Exception {
		CompactDataInput cdi = new CompactDataInput(new ByteArrayInputStream(
				new byte[] { 01, 03, 01, 04 }));

		IProbeArray<?> result = ProbeArrayService.read(
				ExecutionDataWriter.FORMAT_DOUBLEINT_VERSION, cdi);

		assertTrue(result instanceof ProbeDoubleIntArray);
		assertEquals(1, result.length());
		assertTrue(result.isProbeCovered(0));
		assertEquals(3, result.getExecutionProbe(0));
		assertEquals(4, result.getParallelExecutionProbe(0));
	}

	@Test(expected = IOException.class)
	public void read_badType() throws Exception {
		CompactDataInput cdi = new CompactDataInput(new ByteArrayInputStream(
				new byte[] { 04 }));

		ProbeArrayService.read(ExecutionDataWriter.FORMAT_VERSION_UNKNOWN, cdi);
	}

}
