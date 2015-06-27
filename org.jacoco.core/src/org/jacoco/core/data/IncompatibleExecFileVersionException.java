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
package org.jacoco.core.data;

import static java.lang.String.format;

import java.io.IOException;

/**
 * Signals that execution data in an incompatible version was tried to read.
 */
public class IncompatibleExecFileVersionException extends IOException {
	/** Expected format version */
	private final int expectedVersion;

	/** Format version found in execution data */
	private final int actualVersion;

	/**
	 * Creates a new exception to flag version mismatches in exec data
	 *
	 * @param exectedVersion
	 *            version expected in the exec data
	 * @param actualVersion
	 *            version found in the exec data
	 */
	public IncompatibleExecFileVersionException(int expectedVersion,
			int actualVersion) {
		super(String.format("Failed to read version %d data (This "
			+ "JaCoCo build can only read/write version %d "
			+ "data). To read version %d data, use the same JaCoCo "
			+ "version that got used for writing it.",
			actualVersion, expectedVersion, actualVersion));
		this.expectedVersion = expectedVersion;
		this.actualVersion = actualVersion;
	}

	/**
	 * Gets the version expected in the exec data
	 *
	 * @return expected version in exec data
	 */
	public int getExpectedVersion() {
		return expectedVersion;
	}

	/**
	 * Gets the actual version found in the exec data
	 *
	 * @return actual version in exec data
	 */
	public int getActualVersion() {
		return actualVersion;
	}
}
