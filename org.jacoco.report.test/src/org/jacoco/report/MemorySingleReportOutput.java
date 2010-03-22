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
package org.jacoco.report;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * In-memory report output for test purposes.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class MemorySingleReportOutput implements ISingleReportOutput {

	private ByteArrayOutputStream file;

	private boolean closed = false;

	public OutputStream createFile() throws IOException {
		assertNull("Duplicate output.", file);
		file = new ByteArrayOutputStream() {

			@Override
			public void close() throws IOException {
				closed = true;
				super.close();
			}
		};
		return file;
	}

	public byte[] getFile() {
		assertNotNull("Missing file.", file);
		return file.toByteArray();
	}

	public InputStream getFileAsStream() {
		return new ByteArrayInputStream(getFile());
	}

	public void assertClosed() {
		assertTrue(closed);
	}

}
