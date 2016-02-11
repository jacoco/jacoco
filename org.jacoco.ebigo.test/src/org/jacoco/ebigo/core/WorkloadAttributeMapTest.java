/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.ebigo.core;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

@SuppressWarnings("boxing")
public class WorkloadAttributeMapTest {

	@Test
	public void defaultConsturctorIsCorrect() {
		WorkloadAttributeMap instance = new WorkloadAttributeMap();
		assertEquals(instance.size(), 0);
		// assertFalse(instance.isComplete());
	}

	@Test
	public void mapConsturctorIsCorrect() {
		Map<String, Integer> testMap = new HashMap<String, Integer>();
		testMap.put("F1", 3);
		WorkloadAttributeMap instance = new WorkloadAttributeMap(testMap);
		assertEquals(instance.size(), 1);
		assertEquals(instance.get("F1"), new Integer(3));
		// assertFalse(instance.isComplete());
	}
}
