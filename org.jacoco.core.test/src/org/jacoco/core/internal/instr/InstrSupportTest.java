/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * Unit tests for {@link InstrSupport}.
 */
public class InstrSupportTest {

	private Printer printer;
	private TraceMethodVisitor trace;

	@Before
	public void setup() {
		printer = new Textifier();
		trace = new TraceMethodVisitor(printer);
	}

	@Test
	public void testAssertNotIntrumentedPositive() {
		InstrSupport.assertNotInstrumented("run", "Foo");
	}

	@Test(expected = IllegalStateException.class)
	public void testAssertNotIntrumentedField() {
		InstrSupport.assertNotInstrumented("$jacocoData", "Foo");
	}

	@Test(expected = IllegalStateException.class)
	public void testAssertNotIntrumentedMethod() {
		InstrSupport.assertNotInstrumented("$jacocoInit", "Foo");
	}

	@Test
	public void testPushIntM2147483648() {
		InstrSupport.push(trace, -2147483648);
		assertInstruction("LDC -2147483648");
	}

	@Test
	public void testPushIntM32768() {
		InstrSupport.push(trace, -32768);
		assertInstruction("SIPUSH -32768");
	}

	@Test
	public void testPushIntM128() {
		InstrSupport.push(trace, -128);
		assertInstruction("BIPUSH -128");
	}

	@Test
	public void testPushIntM1() {
		InstrSupport.push(trace, -1);
		assertInstruction("ICONST_M1");
	}

	@Test
	public void testPushInt0() {
		InstrSupport.push(trace, 0);
		assertInstruction("ICONST_0");
	}

	@Test
	public void testPushInt1() {
		InstrSupport.push(trace, 1);
		assertInstruction("ICONST_1");
	}

	@Test
	public void testPushInt2() {
		InstrSupport.push(trace, 2);
		assertInstruction("ICONST_2");
	}

	@Test
	public void testPushInt3() {
		InstrSupport.push(trace, 3);
		assertInstruction("ICONST_3");
	}

	@Test
	public void testPushInt4() {
		InstrSupport.push(trace, 4);
		assertInstruction("ICONST_4");
	}

	@Test
	public void testPushInt5() {
		InstrSupport.push(trace, 5);
		assertInstruction("ICONST_5");
	}

	@Test
	public void testPushInt127() {
		InstrSupport.push(trace, 127);
		assertInstruction("BIPUSH 127");
	}

	@Test
	public void testPushInt32767() {
		InstrSupport.push(trace, 32767);
		assertInstruction("SIPUSH 32767");
	}

	@Test
	public void testPushInt2147483647() {
		InstrSupport.push(trace, 2147483647);
		assertInstruction("LDC 2147483647");
	}

	private void assertInstruction(String expected) {
		assertEquals(1, printer.getText().size());
		assertEquals(expected, printer.getText().get(0).toString().trim());
	}

}
