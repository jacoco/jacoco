/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link ProbeCounter}.
 */
public class ProbeCounterTest {

	private ProbeCounter counter;

	@Before
	public void setup() {
		counter = new ProbeCounter();
	}

	@Test
	public void testInitial() {
		assertFalse(counter.hasMethods());
		assertEquals(0, counter.getCount());
	}

	@Test
	public void testVisitTotalProbeCount() {
		counter.visitTotalProbeCount(42);
		assertEquals(42, counter.getCount());
	}

	@Test
	public void testVisitClinitMethod() {
		assertNull(counter.visitMethod(0, "<clinit>", null, null, null));
		assertFalse(counter.hasMethods());
	}

	@Test
	public void testVisitAbstractMethod() {
		counter.visitMethod(Opcodes.ACC_ABSTRACT, "foo", null, null, null);
		assertFalse(counter.hasMethods());
	}

	@Test
	public void testVisitMethod() {
		assertNull(counter.visitMethod(0, "foo", null, null, null));
		assertTrue(counter.hasMethods());
	}

}
