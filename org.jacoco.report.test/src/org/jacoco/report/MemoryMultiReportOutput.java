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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * In-memory report output for test purposes.
 */
public class MemoryMultiReportOutput implements IMultiReportOutput {

	private final Map<String, ByteArrayOutputStream> files = new HashMap<String, ByteArrayOutputStream>();

	private final Set<String> open = new HashSet<String>();

	private boolean closed = false;

	public OutputStream createFile(final String path) throws IOException {
		assertFalse("Duplicate output " + path, files.containsKey(path));
		open.add(path);
		final ByteArrayOutputStream out = new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				open.remove(path);
				super.close();
			}
		};
		files.put(path, out);
		return out;
	}

	public void close() throws IOException {
		closed = true;
	}

	public void assertEmpty() {
		assertEquals(Collections.emptySet(), files.keySet());
	}

	public void assertFile(String path) {
		assertNotNull(String.format("Missing file %s. Actual files are %s.",
				path, files.keySet()), files.get(path));
	}

	public void assertNoFile(String path) {
		assertNull(String.format("Unexpected file %s.", path), files.get(path));
	}

	public void assertSingleFile(String path) {
		assertEquals(Collections.singleton(path), files.keySet());
	}

	public byte[] getFile(String path) {
		assertFile(path);
		return files.get(path).toByteArray();
	}

	public InputStream getFileAsStream(String path) {
		return new ByteArrayInputStream(getFile(path));
	}

	public void assertAllClosed() {
		assertEquals(Collections.emptySet(), open);
		assertTrue(closed);
	}

}
