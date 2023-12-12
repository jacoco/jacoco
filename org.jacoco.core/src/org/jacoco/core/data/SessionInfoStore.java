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

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container to collect and merge session {@link SessionInfo} objects. A
 * instance of this class is not thread safe.
 */
public class SessionInfoStore implements ISessionInfoVisitor {

	private final List<SessionInfo> infos = new ArrayList<SessionInfo>();

	/**
	 * Tests whether the store is empty.
	 *
	 * @return <code>true</code> if the store is empty
	 */
	public boolean isEmpty() {
		return infos.isEmpty();
	}

	/**
	 * Returns all {@link SessionInfo} objects currently contained in the store.
	 * The info objects are ordered by its natural ordering (i.e. by the dump
	 * time stamp).
	 *
	 * @return list of stored {@link SessionInfo} objects
	 */
	public List<SessionInfo> getInfos() {
		final List<SessionInfo> copy = new ArrayList<SessionInfo>(infos);
		Collections.sort(copy);
		return copy;
	}

	/**
	 * Returns a new session info with the given id that contains a merged
	 * version from all contained version. The start timestamp is the minimum of
	 * all contained sessions, the dump timestamp the maximum of all contained
	 * sessions. If no session is currently contained both timestamps are set to
	 * <code>0</code>.
	 *
	 * @param id
	 *            identifier for the merged session info
	 * @return new {@link SessionInfo} object
	 *
	 */
	public SessionInfo getMerged(final String id) {
		if (infos.isEmpty()) {
			return new SessionInfo(id, 0, 0);
		}
		long start = Long.MAX_VALUE;
		long dump = Long.MIN_VALUE;
		for (final SessionInfo i : infos) {
			start = min(start, i.getStartTimeStamp());
			dump = max(dump, i.getDumpTimeStamp());
		}
		return new SessionInfo(id, start, dump);
	}

	/**
	 * Writes all contained {@link SessionInfo} objects into the given visitor.
	 * The info objects are emitted in chronological order by dump timestamp.
	 *
	 * @param visitor
	 *            visitor to emit {@link SessionInfo} objects to
	 */
	public void accept(final ISessionInfoVisitor visitor) {
		for (final SessionInfo i : getInfos()) {
			visitor.visitSessionInfo(i);
		}
	}

	// === ISessionInfoVisitor ===

	public void visitSessionInfo(final SessionInfo info) {
		infos.add(info);
	}

}
