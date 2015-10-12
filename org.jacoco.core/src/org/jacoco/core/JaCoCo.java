/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

import java.util.ResourceBundle;

import org.objectweb.asm.Opcodes;

/**
 * Static Meta information about JaCoCo.
 */
public final class JaCoCo {

	/** Qualified build version of the JaCoCo core library. */
	public static final String VERSION;

	/** Absolute URL of the current JaCoCo home page */
	public static final String HOMEURL;

	/** Name of the runtime package of this build */
	public static final String RUNTIMEPACKAGE;

	/** ASM API version */
	public static final int ASM_API_VERSION = Opcodes.ASM5;

	static {
		final ResourceBundle bundle = ResourceBundle
				.getBundle("org.jacoco.core.jacoco");
		VERSION = bundle.getString("VERSION");
		HOMEURL = bundle.getString("HOMEURL");
		final String runtimepackage = bundle.getString("RUNTIMEPACKAGE");
		// This is so when we run Unit Tests in an IDE, this value is not yet
		// updated.
		RUNTIMEPACKAGE = runtimepackage != null
				&& !runtimepackage.equals("$jacoco.runtime.package.name$") ? runtimepackage
				: "org.jacoco";
	}

	private JaCoCo() {
	}

}
