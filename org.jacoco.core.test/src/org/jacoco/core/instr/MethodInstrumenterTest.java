/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.instr;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link MethodInstrumenter}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class MethodInstrumenterTest {

	private IProbeArrayStrategy probeArrayStrategy;

	private MethodInstrumenter instrumenter;

	private MethodRecorder expected;

	private MethodRecorder actual;

	@Before
	public void setup() {
		actual = new MethodRecorder();
		expected = new MethodRecorder();
		probeArrayStrategy = new IProbeArrayStrategy() {
			public int pushInstance(MethodVisitor mv) {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Target",
						"$jacocoInit", "()[Z");
				return 1;
			}

			public void addMembers(ClassVisitor delegate, int probeCount) {
			}
		};
		instrumenter = new MethodInstrumenter(actual, 0, "()V",
				probeArrayStrategy);
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
