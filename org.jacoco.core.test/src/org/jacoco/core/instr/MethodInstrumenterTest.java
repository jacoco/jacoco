/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.instr;

import static org.junit.Assert.assertEquals;

import org.jacoco.core.test.MethodRecorder;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link MethodInstrumenter}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class MethodInstrumenterTest {

	private MethodInstrumenter instrumenter;

	private MethodRecorder expected;

	private MethodRecorder actual;

	@Before
	public void setup() {
		actual = new MethodRecorder();
		expected = new MethodRecorder();
		instrumenter = new MethodInstrumenter(actual, 0, "test", "()V",
				"Target");
	}

	void sampleReturn() {
		return;
	}

	@Test
	public void testReturn() {
		instrumenter.visitCode();
		instrumenter.visitBlockEndBeforeJump(0);
		instrumenter.visitInsn(Opcodes.RETURN);
		instrumenter.visitBlockEnd(0);
		instrumenter.visitMaxs(0, 1);
		instrumenter.visitEnd();

		expected.visitCode();
		expected.visitMethodInsn(Opcodes.INVOKESTATIC, "Target", "$jacocoInit",
				"()[Z");
		expected.visitVarInsn(Opcodes.ASTORE, 1);
		expected.visitVarInsn(Opcodes.ALOAD, 1);
		expected.visitInsn(Opcodes.ICONST_0);
		expected.visitInsn(Opcodes.ICONST_1);
		expected.visitInsn(Opcodes.BASTORE);
		expected.visitInsn(Opcodes.RETURN);
		expected.visitMaxs(3, 2);
		expected.visitEnd();

		assertEquals(expected, actual);
	}
}
