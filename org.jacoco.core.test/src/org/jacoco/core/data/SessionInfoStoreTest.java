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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link SessionInfoStore}.
 */
public class SessionInfoStoreTest {

	private SessionInfoStore store;

	@Before
	public void setup() {
		store = new SessionInfoStore();
	}

	@Test
	public void testEmpty() {
		assertEquals(Collections.emptyList(), store.getInfos());
		assertTrue(store.isEmpty());
	}

	@Test
	public void testIsEmpty() {
		store.visitSessionInfo(new SessionInfo("A", 123, 456));
		assertFalse(store.isEmpty());
	}

	@Test
	public void testGetInfos() {
		final SessionInfo a = new SessionInfo("A", 12345, 500000);
		store.visitSessionInfo(a);
		final SessionInfo b = new SessionInfo("B", 12345, 400000);
		store.visitSessionInfo(b);
		final SessionInfo c = new SessionInfo("C", 12345, 600000);
		store.visitSessionInfo(c);
		assertEquals(Arrays.asList(b, a, c), store.getInfos());
	}

	@Test
	public void testGetMergedEmpty() {
		final SessionInfo info = store.getMerged("MERGE");
		assertEquals("MERGE", info.getId());
		assertEquals(0, info.getStartTimeStamp());
		assertEquals(0, info.getDumpTimeStamp());
	}

	@Test
	public void testGetMerged() {
		store.visitSessionInfo(new SessionInfo("A", 23456, 500000));
		store.visitSessionInfo(new SessionInfo("B", 12345, 400000));
		store.visitSessionInfo(new SessionInfo("C", 34567, 600000));
		final SessionInfo info = store.getMerged("MERGE");
		assertEquals("MERGE", info.getId());
		assertEquals(12345, info.getStartTimeStamp());
		assertEquals(600000, info.getDumpTimeStamp());
	}

	@Test
	public void testAccept() {
		final SessionInfo a = new SessionInfo("A", 12345, 500000);
		store.visitSessionInfo(a);
		final SessionInfo b = new SessionInfo("B", 12345, 400000);
		store.visitSessionInfo(b);
		final SessionInfo c = new SessionInfo("C", 12345, 600000);
		store.visitSessionInfo(c);
		final List<SessionInfo> actual = new ArrayList<SessionInfo>();
		store.accept(new ISessionInfoVisitor() {
			public void visitSessionInfo(SessionInfo info) {
				actual.add(info);
			}
		});
		assertEquals(Arrays.asList(b, a, c), actual);
	}

}
