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

import org.junit.Test;

public class WorkloadAttributeMapBuilderTest {

	@Test
	public void testStaticCreate() {
		WorkloadAttributeMapBuilder instance = WorkloadAttributeMapBuilder
				.create();
		WorkloadAttributeMap map = instance.build();
		assertEquals(map.size(), 0);
	}

	@Test
	public void testStaticCreateWithAdd() {
		WorkloadAttributeMapBuilder instance = WorkloadAttributeMapBuilder
				.create("KEY", 10);
		WorkloadAttributeMap map = instance.build();
		assertEquals(1, map.size());
		Integer integer = map.get("KEY");
		assertEquals(10, integer.intValue());
	}

	@Test
	public void testAdd() {
		WorkloadAttributeMapBuilder builder = WorkloadAttributeMapBuilder
				.create();
		builder.add("KEY", 10);
		WorkloadAttributeMap map = builder.build();
		assertEquals(1, map.size());
		Integer integer = map.get("KEY");
		assertEquals(10, integer.intValue());
	}
}