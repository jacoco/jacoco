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
package org.jacoco.core.data;

/**
 * Data object describing a session which was the source of execution data.
 * {@link SessionInfo} instances can be sorted by dump date through the
 * {@link Comparable} interface.
 */
public class SessionInfo implements Comparable<SessionInfo> {

	private final String id;

	private final long start;

	private final long dump;

	/**
	 * Create a immutable session info with the given data.
	 *
	 * @param id
	 *            arbitrary session identifier, must not be <code>null</code>
	 * @param start
	 *            the epoc based time stamp when execution data recording has
	 *            been started
	 * @param dump
	 *            the epoc based time stamp when execution data was collected
	 */
	public SessionInfo(final String id, final long start, final long dump) {
		if (id == null) {
			throw new IllegalArgumentException();
		}
		this.id = id;
		this.start = start;
		this.dump = dump;
	}

	/**
	 * @return identifier for this session
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the epoc based time stamp when execution data recording has been
	 *         started
	 */
	public long getStartTimeStamp() {
		return start;
	}

	/**
	 * @return the epoc based time stamp when execution data was collected
	 */
	public long getDumpTimeStamp() {
		return dump;
	}

	public int compareTo(final SessionInfo other) {
		if (this.dump < other.dump) {
			return -1;
		}
		if (this.dump > other.dump) {
			return +1;
		}
		return 0;
	}

	@Override
	public String toString() {
		return "SessionInfo[" + id + "]";
	}
}
