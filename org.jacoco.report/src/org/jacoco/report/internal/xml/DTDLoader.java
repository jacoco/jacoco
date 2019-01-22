/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.internal.xml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Internal utility to load the report DTD.
 */
public class DTDLoader {

	private DTDLoader() {
	}

	/**
	 * @return The DTD as a string
	 * @throws IOException
	 *             in case the DTD cannot be loaded
	 */
	public static String load() throws IOException {
		final StringBuilder sb = new StringBuilder();
		final Reader in = new InputStreamReader(DTDLoader.class
				.getResourceAsStream("/org/jacoco/report/xml/report.dtd"),
				"UTF-8");
		try {
			int c;
			while ((c = in.read()) != -1) {
				sb.append((char) c);
			}
		} finally {
			in.close();
		}
		return sb.toString();
	}

}
