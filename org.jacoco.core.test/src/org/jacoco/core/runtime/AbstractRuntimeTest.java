/*******************************************************************************
 *  Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.runtime;

import static org.junit.Assert.assertTrue;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

public class AbstractRuntimeTest {
	@Test
	public void testCreateRandomId() {
		final SortedSet<String> previousIdSet = new TreeSet<String>();
		for (int i = 0; i < 100; i++) {
			final String id = AbstractRuntime.createRandomId();
			assertTrue(previousIdSet.add(id));
		}
	}
}
