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

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link IncompatibleExecDataVersionExceptionTest}.
 */
public class IncompatibleExecDataVersionExceptionTest {

	private IncompatibleExecDataVersionException exception;

	@Before
	public void setup() {
		exception = new IncompatibleExecDataVersionException(0x1234);
	}

	@Test
	public void testGetMessage() {
		String expected = "Cannot read execution data version 0x1234. "
				+ "This version of JaCoCo uses execution data version 0x"
				+ Integer.toHexString(ExecutionDataWriter.FORMAT_VERSION) + ".";
		assertEquals(expected, exception.getMessage());
	}

	@Test
	public void testGetActualVersion() {
		assertEquals(0x1234, exception.getActualVersion());
	}

	@Test
	public void testGetExpectedVersion() {
		assertEquals(ExecutionDataWriter.FORMAT_VERSION,
				exception.getExpectedVersion());
	}
}
