/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Static Meta information about JaCoCo.
 */
public final class JaCoCo {

	/** Qualified build version of the JaCoCo core library. */
	public static final String VERSION;

	/** Absolute URL of the current JaCoCo home page */
	public static final String HOMEURL;

	static {
		final Properties properties = new Properties();
		try {
			final InputStream in = JaCoCo.class
					.getResourceAsStream("jacoco.properties");
			properties.load(in);
			in.close();
		} catch (final IOException e) {
			throw new AssertionError(e);
		}
		VERSION = properties.getProperty("VERSION");
		HOMEURL = properties.getProperty("HOMEURL");
	}

	private JaCoCo() {
	}

}
