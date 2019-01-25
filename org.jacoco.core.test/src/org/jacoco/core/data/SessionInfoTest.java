/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link SessionInfo}.
 */
public class SessionInfoTest {

	@Test(expected = IllegalArgumentException.class)
	public void testNullId() {
		new SessionInfo(null, 1, 2);
	}

	@Test
	public void testGetters() {
		final SessionInfo info = new SessionInfo("id", 1000, 2000);
		assertEquals("id", info.getId());
		assertEquals(1000, info.getStartTimeStamp());
		assertEquals(2000, info.getDumpTimeStamp());
	}

	@Test
	public void testCompare() {
		assertEquals(0,
				new SessionInfo("id", 1000, 2000).compareTo(new SessionInfo(
						"id", 1234, 2000)));
		assertEquals(-1,
				new SessionInfo("id", 3333, 1999).compareTo(new SessionInfo(
						"id", 1234, 2000)));
		assertEquals(+1,
				new SessionInfo("id", 1234, 2001).compareTo(new SessionInfo(
						"id", 2222, 2000)));
	}

	@Test
	public void testToString() {
		assertEquals("SessionInfo[id]",
				new SessionInfo("id", 1000, 2000).toString());
	}

}
