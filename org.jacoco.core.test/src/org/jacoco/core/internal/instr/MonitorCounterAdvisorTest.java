/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.data.ProbeMode;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.junit.After;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

public class MonitorCounterAdvisorTest {
	private class MockMethodVisitor extends MethodProbesVisitor {
		private int opcode = Opcodes.NOP;
		private String name;
		private String owner;
		private int visitInsn = Opcodes.NOP;
		private Boolean visitInsnFirst = null;

		@Override
		public void visitMethodInsn(int opcode, String owner, String name,
				String desc, boolean itf) {
			this.opcode = opcode;
			this.owner = owner;
			this.name = name;
			if (visitInsnFirst == null) {
				visitInsnFirst = Boolean.FALSE;
			}
		}

		@Override
		public void visitInsn(int opcode) {
			this.visitInsn = opcode;
			if (visitInsnFirst == null) {
				visitInsnFirst = Boolean.TRUE;
			}
		}

	}

	@After
	public void cleanupProbeMode() {
		ProbeArrayService.reset();
	}

	@Test
	public void testProbeModeExists() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.exists);
		MockMethodVisitor mv = new MockMethodVisitor();

		MonitorCounterAdvisor instance = new MonitorCounterAdvisor(
				JaCoCo.ASM_API_VERSION, mv, 0, "TestMethod", "()V");
		instance.onMethodEnter();
		instance.onMethodExit(Opcodes.NOP);
		instance.visitInsn(Opcodes.AALOAD); // not a monitor, entry, or exit

		assertFalse(instance.isShouldMonitor());
		assertFalse(instance.isShouldWrap());
		assertEquals(Opcodes.NOP, mv.opcode);
	}

	@Test
	public void testProbeModeCount() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.count);
		MockMethodVisitor mv = new MockMethodVisitor();

		MonitorCounterAdvisor instance = new MonitorCounterAdvisor(
				JaCoCo.ASM_API_VERSION, mv, 0, "TestMethod", "()V");
		instance.onMethodEnter();
		instance.onMethodExit(Opcodes.NOP);
		instance.visitInsn(Opcodes.AALOAD); // not a monitor, entry, or exit

		assertFalse(instance.isShouldMonitor());
		assertFalse(instance.isShouldWrap());
		assertEquals(Opcodes.NOP, mv.opcode);
	}

	@Test
	public void testProbeModeParallel_notSynced() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.parallelcount);
		MockMethodVisitor mv = new MockMethodVisitor();

		MonitorCounterAdvisor instance = new MonitorCounterAdvisor(
				JaCoCo.ASM_API_VERSION, mv, 0, "TestMethod", "()V");
		instance.onMethodEnter();
		instance.onMethodExit(Opcodes.NOP);
		instance.visitInsn(Opcodes.AALOAD); // not a monitor, entry, or exit

		assertTrue(instance.isShouldMonitor());
		assertFalse(instance.isShouldWrap());
		assertEquals(Opcodes.NOP, mv.opcode);
	}

	@Test
	public void testProbeModeParallel_Synced() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.parallelcount);
		MockMethodVisitor mv = new MockMethodVisitor();

		MonitorCounterAdvisor instance = new MonitorCounterAdvisor(
				JaCoCo.ASM_API_VERSION, mv, Opcodes.ACC_SYNCHRONIZED,
				"TestMethod", "()V");
		instance.onMethodEnter();
		instance.onMethodExit(Opcodes.NOP);
		instance.visitInsn(Opcodes.AALOAD); // not a monitor, entry, or exit

		assertTrue(instance.isShouldMonitor());
		assertTrue(instance.isShouldWrap());
		assertEquals(Opcodes.INVOKESTATIC, mv.opcode);
	}

	@Test
	public void testIncrementOnEnter() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.parallelcount);
		MockMethodVisitor mv = new MockMethodVisitor();

		MonitorCounterAdvisor instance = new MonitorCounterAdvisor(
				JaCoCo.ASM_API_VERSION, mv, Opcodes.ACC_SYNCHRONIZED,
				"TestMethod", "()V");
		instance.onMethodEnter();

		assertEquals(MonitorCounterAdvisor.COUNTER_CLASS_NAME, mv.owner);
		assertEquals("increment", mv.name);
	}

	@Test
	public void testDecrementOnExit() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.parallelcount);
		MockMethodVisitor mv = new MockMethodVisitor();

		MonitorCounterAdvisor instance = new MonitorCounterAdvisor(
				JaCoCo.ASM_API_VERSION, mv, Opcodes.ACC_SYNCHRONIZED,
				"TestMethod", "()V");
		instance.onMethodExit(Opcodes.NOP);

		assertEquals(MonitorCounterAdvisor.COUNTER_CLASS_NAME, mv.owner);
		assertEquals("decrement", mv.name);
	}

	@Test
	public void visitInsn_MonitorEnter() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.parallelcount);
		MockMethodVisitor mv = new MockMethodVisitor();

		MonitorCounterAdvisor instance = new MonitorCounterAdvisor(
				JaCoCo.ASM_API_VERSION, mv, 0, "TestMethod", "()V");
		instance.visitInsn(Opcodes.MONITORENTER);

		assertEquals(MonitorCounterAdvisor.COUNTER_CLASS_NAME, mv.owner);
		assertEquals("increment", mv.name);
		assertEquals(Opcodes.MONITORENTER, mv.visitInsn);
		assertFalse(mv.visitInsnFirst.booleanValue());
	}

	@Test
	public void visitInsn_MonitorExit() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.parallelcount);
		MockMethodVisitor mv = new MockMethodVisitor();

		MonitorCounterAdvisor instance = new MonitorCounterAdvisor(
				JaCoCo.ASM_API_VERSION, mv, Opcodes.ACC_SYNCHRONIZED,
				"TestMethod", "()V");
		instance.visitInsn(Opcodes.MONITOREXIT);

		assertEquals(MonitorCounterAdvisor.COUNTER_CLASS_NAME, mv.owner);
		assertEquals("decrement", mv.name);
		assertEquals(Opcodes.MONITOREXIT, mv.visitInsn);
		assertTrue(mv.visitInsnFirst.booleanValue());
	}

	@Test
	public void visitInsn_MonitorOther() {
		ProbeArrayService.reset();
		ProbeArrayService.configure(ProbeMode.parallelcount);
		MockMethodVisitor mv = new MockMethodVisitor();

		MonitorCounterAdvisor instance = new MonitorCounterAdvisor(
				JaCoCo.ASM_API_VERSION, mv, Opcodes.ACC_SYNCHRONIZED,
				"TestMethod", "()V");
		instance.visitInsn(Opcodes.AALOAD); // not a monitor, entry, or exit

		assertNull(mv.owner);
		assertNull(mv.name);
		assertEquals(Opcodes.AALOAD, mv.visitInsn);
		assertTrue(mv.visitInsnFirst.booleanValue());
	}
}
