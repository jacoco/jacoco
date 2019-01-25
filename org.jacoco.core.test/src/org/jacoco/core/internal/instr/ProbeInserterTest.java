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
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertEquals;

import org.jacoco.core.instr.MethodRecorder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link ProbeInserter}.
 */
public class ProbeInserterTest {

	private MethodRecorder actual, expected;

	private MethodVisitor actualVisitor, expectedVisitor;

	private IProbeArrayStrategy arrayStrategy;

	@Before
	public void setup() {
		actual = new MethodRecorder();
		actualVisitor = actual.getVisitor();
		expected = new MethodRecorder();
		expectedVisitor = expected.getVisitor();
		arrayStrategy = new IProbeArrayStrategy() {
			public int storeInstance(MethodVisitor mv, boolean clinit, int variable) {
				mv.visitLdcInsn(clinit ? "clinit" : "init");
				return 5;
			}

			public void addMembers(ClassVisitor delegate, int probeCount) {
			}
		};
	}

	@After
	public void verify() {
		assertEquals(expected, actual);
	}

	@Test
	public void testVariableStatic() {
		ProbeInserter pi = new ProbeInserter(Opcodes.ACC_STATIC, "m", "()V",
				actualVisitor, arrayStrategy);
		pi.insertProbe(0);

		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 0);
		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitInsn(Opcodes.ICONST_1);
		expectedVisitor.visitInsn(Opcodes.BASTORE);
	}

	@Test
	public void testVariableNonStatic() {
		ProbeInserter pi = new ProbeInserter(0, "m", "()V", actualVisitor,
				arrayStrategy);
		pi.insertProbe(0);

		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 1);
		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitInsn(Opcodes.ICONST_1);
		expectedVisitor.visitInsn(Opcodes.BASTORE);
	}

	@Test
	public void testVariableNonStatic_IZObject() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(IZLjava/lang/Object;)V",
				actualVisitor, arrayStrategy);
		pi.insertProbe(0);

		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 4);
		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitInsn(Opcodes.ICONST_1);
		expectedVisitor.visitInsn(Opcodes.BASTORE);
	}

	@Test
	public void testVariableNonStatic_JD() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(JD)V", actualVisitor,
				arrayStrategy);
		pi.insertProbe(0);

		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 5);
		expectedVisitor.visitInsn(Opcodes.ICONST_0);
		expectedVisitor.visitInsn(Opcodes.ICONST_1);
		expectedVisitor.visitInsn(Opcodes.BASTORE);
	}

	@Test
	public void testVisitCode() {
		ProbeInserter pi = new ProbeInserter(0, "m", "()V", actualVisitor,
				arrayStrategy);
		pi.visitCode();

		expectedVisitor.visitLdcInsn("init");
	}

	@Test
	public void testVisitClinit() {
		ProbeInserter pi = new ProbeInserter(0, "<clinit>", "()V",
				actualVisitor, arrayStrategy);
		pi.visitCode();

		expectedVisitor.visitLdcInsn("clinit");
	}

	@Test
	public void testVisitVarIns() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(II)V", actualVisitor,
				arrayStrategy);

		pi.visitVarInsn(Opcodes.ALOAD, 0);
		pi.visitVarInsn(Opcodes.ILOAD, 1);
		pi.visitVarInsn(Opcodes.ILOAD, 2);
		pi.visitVarInsn(Opcodes.ISTORE, 3);
		pi.visitVarInsn(Opcodes.FSTORE, 4);

		// Argument variables stay at the same position:
		expectedVisitor.visitVarInsn(Opcodes.ALOAD, 0);
		expectedVisitor.visitVarInsn(Opcodes.ILOAD, 1);
		expectedVisitor.visitVarInsn(Opcodes.ILOAD, 2);

		// Local variables are shifted by one:
		expectedVisitor.visitVarInsn(Opcodes.ISTORE, 4);
		expectedVisitor.visitVarInsn(Opcodes.FSTORE, 5);
	}

	@Test
	public void testVisitIincInsn() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(II)V", actualVisitor,
				arrayStrategy);
		pi.visitIincInsn(0, 100);
		pi.visitIincInsn(1, 101);
		pi.visitIincInsn(2, 102);
		pi.visitIincInsn(3, 103);
		pi.visitIincInsn(4, 104);

		// Argument variables stay at the same position:
		expectedVisitor.visitIincInsn(0, 100);
		expectedVisitor.visitIincInsn(1, 101);
		expectedVisitor.visitIincInsn(2, 102);

		// Local variables are shifted by one:
		expectedVisitor.visitIincInsn(4, 103);
		expectedVisitor.visitIincInsn(5, 104);
	}

	@Test
	public void testVisitLocalVariable() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(II)V", actualVisitor,
				arrayStrategy);

		pi.visitLocalVariable(null, null, null, null, null, 0);
		pi.visitLocalVariable(null, null, null, null, null, 1);
		pi.visitLocalVariable(null, null, null, null, null, 2);
		pi.visitLocalVariable(null, null, null, null, null, 3);
		pi.visitLocalVariable(null, null, null, null, null, 4);

		// Argument variables stay at the same position:
		expectedVisitor.visitLocalVariable(null, null, null, null, null, 0);
		expectedVisitor.visitLocalVariable(null, null, null, null, null, 1);
		expectedVisitor.visitLocalVariable(null, null, null, null, null, 2);

		// Local variables are shifted by one:
		expectedVisitor.visitLocalVariable(null, null, null, null, null, 4);
		expectedVisitor.visitLocalVariable(null, null, null, null, null, 5);
	}

	@Test
	public void testVisitMaxs1() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(II)V", actualVisitor,
				arrayStrategy);
		pi.visitCode();
		pi.visitMaxs(0, 8);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitMaxs(5, 9);
	}

	@Test
	public void testVisitMaxs2() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(II)V", actualVisitor,
				arrayStrategy);
		pi.visitCode();
		pi.visitMaxs(10, 8);

		expectedVisitor.visitLdcInsn("init");
		expectedVisitor.visitMaxs(13, 9);
	}

	@Test
	public void testVisitFrame() {
		ProbeInserter pi = new ProbeInserter(0, "m", "(J)V", actualVisitor,
				arrayStrategy);

		pi.visitFrame(Opcodes.F_NEW, 3, new Object[] { "Foo", Opcodes.LONG,
				"java/lang/String" }, 0, new Object[0]);

		expectedVisitor.visitFrame(Opcodes.F_NEW, 4, new Object[] { "Foo",
				Opcodes.LONG, "[Z", "java/lang/String" }, 0, new Object[0]);
	}

	@Test
	public void testVisitFrameNoLocals() {
		ProbeInserter pi = new ProbeInserter(Opcodes.ACC_STATIC, "m", "()V",
				actualVisitor, arrayStrategy);

		pi.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[0]);

		expectedVisitor.visitFrame(Opcodes.F_NEW, 1, new Object[] { "[Z" }, 0,
				new Object[0]);
	}

	@Test
	public void testVisitFrameProbeAt0() {
		ProbeInserter pi = new ProbeInserter(Opcodes.ACC_STATIC, "m", "()V",
				actualVisitor, arrayStrategy);

		pi.visitFrame(Opcodes.F_NEW, 2, new Object[] { Opcodes.DOUBLE, "Foo" },
				0, new Object[0]);

		expectedVisitor.visitFrame(Opcodes.F_NEW, 3, new Object[] { "[Z",
				Opcodes.DOUBLE, "Foo" }, 0, new Object[0]);
	}

	@Test
	public void testFillOneWord() {
		ProbeInserter pi = new ProbeInserter(Opcodes.ACC_STATIC, "m", "(I)V",
				actualVisitor, arrayStrategy);

		pi.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});

		// The locals in this frame are filled with TOP up to the probe variable
		expectedVisitor.visitFrame(Opcodes.F_NEW, 2, new Object[] {
				Opcodes.TOP, "[Z", }, 0, new Object[] {});
	}

	@Test
	public void testFillTwoWord() {
		ProbeInserter pi = new ProbeInserter(Opcodes.ACC_STATIC, "m", "(J)V",
				actualVisitor, arrayStrategy);

		pi.visitFrame(Opcodes.F_NEW, 0, new Object[] {}, 0, new Object[] {});

		// The locals in this frame are filled with TOP up to the probe variable
		expectedVisitor.visitFrame(Opcodes.F_NEW, 3, new Object[] {
				Opcodes.TOP, Opcodes.TOP, "[Z", }, 0, new Object[] {});
	}

	@Test
	public void testFillPartly() {
		ProbeInserter pi = new ProbeInserter(Opcodes.ACC_STATIC, "m", "(DIJ)V",
				actualVisitor, arrayStrategy);

		pi.visitFrame(Opcodes.F_NEW, 1, new Object[] { Opcodes.DOUBLE }, 0,
				new Object[] {});

		// The locals in this frame are filled with TOP up to the probe variable
		expectedVisitor.visitFrame(Opcodes.F_NEW, 5, new Object[] {
				Opcodes.DOUBLE, Opcodes.TOP, Opcodes.TOP, Opcodes.TOP, "[Z", },
				0, new Object[] {});
	}

	@Test(expected = IllegalArgumentException.class)
	public void testVisitFrame_invalidType() {
		ProbeInserter pi = new ProbeInserter(0, "m", "()V", actualVisitor,
				arrayStrategy);
		pi.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
	}

}
