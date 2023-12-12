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
package org.jacoco.core.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;

class TestStorage implements IExecutionDataVisitor, ISessionInfoVisitor {

	private final Map<Long, ExecutionData> data = new HashMap<Long, ExecutionData>();

	private SessionInfo info;

	public void assertSize(int size) {
		assertEquals(size, data.size(), 0.0);
	}

	public ExecutionData getData(long classId) {
		return data.get(Long.valueOf(classId));
	}

	public SessionInfo getSessionInfo() {
		return info;
	}

	public void assertData(long classId, boolean[] expected) {
		assertSame(expected, getData(classId).getProbes());
	}

	// === ICoverageDataVisitor ===

	public void visitClassExecution(final ExecutionData ed) {
		data.put(Long.valueOf(ed.getId()), ed);
	}

	// === ISessionInfoVisitor ===

	public void visitSessionInfo(SessionInfo info) {
		this.info = info;
	}

}
