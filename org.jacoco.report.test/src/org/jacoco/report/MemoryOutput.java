/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
