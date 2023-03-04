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
package org.jacoco.core;

import java.util.ResourceBundle;

/**
 * Static Meta information about JaCoCo.
 */
public final class JaCoCo {

	/** Qualified version of JaCoCo core. */
	public static final String VERSION;

	/** Commit ID of the source tree of JaCoCo core. */
	public static final String COMMITID;

	/**
	 * Shortened (7 digit) commit ID of the source tree of JaCoCo core.
	 */
	public static final String COMMITID_SHORT;

	/** Absolute URL of the current JaCoCo home page */
	public static final String HOMEURL;

	/** Name of the runtime package of this build */
	public static final String RUNTIMEPACKAGE;

	static {
		final ResourceBundle bundle = ResourceBundle
				.getBundle("org.jacoco.core.jacoco");
		VERSION = bundle.getString("VERSION");
		COMMITID = bundle.getString("COMMITID");
		COMMITID_SHORT = COMMITID.substring(0, 7);
		HOMEURL = bundle.getString("HOMEURL");
		RUNTIMEPACKAGE = bundle.getString("RUNTIMEPACKAGE");
	}

	private JaCoCo() {
	}

}
