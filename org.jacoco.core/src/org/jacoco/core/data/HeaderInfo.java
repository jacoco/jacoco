/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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

/**
 * Data object describing the header info for a set of session and execution
 * data info.
 */
public class HeaderInfo implements Comparable<HeaderInfo> {

	private final char formatVersion;

	/**
	 * Create a immutable header with the given data.
	 * 
	 * @param formatVersion
	 *            the version of the execution data
	 */
	public HeaderInfo(final char formatVersion) {
		this.formatVersion = formatVersion;
	}

	/**
	 * @return the format version
	 */
	public char getFormatVersion() {
		return formatVersion;
	}

	public int compareTo(final HeaderInfo other) {
		return Character.compare(this.formatVersion, other.formatVersion);
	}

	@Override
	public String toString() {
		return "Header[0x" + Integer.toHexString(formatVersion) + "]";
	}
}
