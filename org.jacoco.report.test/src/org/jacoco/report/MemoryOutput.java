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
package org.jacoco.report;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * In-memory report output for test purposes.
 */
public class MemoryOutput extends ByteArrayOutputStream {

	private boolean closed = false;

	@Override
	public void close() throws IOException {
		super.close();
		closed = true;
	}

	public InputStream getContentsAsStream() {
		return new ByteArrayInputStream(toByteArray());
	}

	public void assertClosed() {
		assertTrue(closed);
	}

}
