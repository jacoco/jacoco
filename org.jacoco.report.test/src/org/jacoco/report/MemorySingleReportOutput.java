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
package org.jacoco.report;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * In-memory report output for test purposes.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class MemorySingleReportOutput implements ISingleReportOutput {

	private ByteArrayOutputStream file;

	public OutputStream createFile() throws IOException {
		assertNull("Duplicate output.", file);
		file = new ByteArrayOutputStream();
		return file;
	}

	public byte[] getFile() {
		assertNotNull("Missing file.", file);
		return file.toByteArray();
	}
}
