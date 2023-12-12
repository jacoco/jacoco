/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann, somechris - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.data;

import java.io.IOException;

/**
 * Signals that execution data in an incompatible version was tried to read.
 */
public class IncompatibleExecDataVersionException extends IOException {

	private static final long serialVersionUID = 1L;

	private final int actualVersion;

	/**
	 * Creates a new exception to flag version mismatches in execution data.
	 *
	 * @param actualVersion
	 *            version found in the exec data
	 */
	public IncompatibleExecDataVersionException(final int actualVersion) {
		super(String.format("Cannot read execution data version 0x%x. "
				+ "This version of JaCoCo uses execution data version 0x%x.",
				Integer.valueOf(actualVersion),
				Integer.valueOf(ExecutionDataWriter.FORMAT_VERSION)));
		this.actualVersion = actualVersion;
	}

	/**
	 * Gets the version expected in the execution data which can be read by this
	 * version of JaCoCo.
	 *
	 * @return expected version in execution data
	 */
	public int getExpectedVersion() {
		return ExecutionDataWriter.FORMAT_VERSION;
	}

	/**
	 * Gets the actual version found in the execution data.
	 *
	 * @return actual version in execution data
	 */
	public int getActualVersion() {
		return actualVersion;
	}

}
