/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.SystemPropertiesRuntime;
import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.validation.java5.targets.StructuredLockingTarget;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

/**
 * Tests that the invariants specified in <a href=
 * "https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-2.html#jvms-2.11.10">chapter
 * 2.11.10 of the JVM Spec</a> do also hold for instrumented classes.
 *
 * This is important because JIT compiler in HotSpot JVM ignores methods with
 * unstructured locking, so that they executed by interpreter. Android Runtime
 * also doesn't optimize such methods.
 *
 * TODO verification implemented here is incomplete - in particular it is unable
 * to catch problem described in https://github.com/jacoco/jacoco/issues/626
 */
public class StructuredLockingTest {

	@Test
	public void testMonitorExit() throws Exception {
		assertStructuredLocking(TargetLoader
				.getClassDataAsBytes(StructuredLockingTarget.class));
	}

	private void assertStructuredLocking(byte[] source) throws Exception {
		IRuntime runtime = new SystemPropertiesRuntime();
		Instrumenter instrumenter = new Instrumenter(runtime);
		byte[] instrumented = instrumenter.instrument(source, "TestTarget");

		ClassNode cn = new ClassNode();
		InstrSupport.classReaderFor(instrumented).accept(cn, 0);
		for (MethodNode mn : cn.methods) {
			assertStructuredLocking(cn.name, mn);
		}
	}

	private void assertStructuredLocking(String owner, MethodNode mn)
			throws Exception {
		Analyzer<BasicValue> analyzer = new Analyzer<BasicValue>(
				new BasicInterpreter()) {

			@Override
			protected Frame<BasicValue> newFrame(int nLocals, int nStack) {
				return new LockFrame(nLocals, nStack);
			}

			@Override
			protected Frame<BasicValue> newFrame(
					Frame<? extends BasicValue> src) {
				return new LockFrame(src);
			}
		};

		Frame<BasicValue>[] frames = analyzer.analyze(owner, mn);

		// Make sure no locks are left when method exits:
		for (int i = 0; i < frames.length; i++) {
			AbstractInsnNode insn = mn.instructions.get(i);
			switch (insn.getOpcode()) {
			case Opcodes.IRETURN:
			case Opcodes.LRETURN:
			case Opcodes.FRETURN:
			case Opcodes.DRETURN:
			case Opcodes.ARETURN:
			case Opcodes.RETURN:
				((LockFrame) frames[i]).assertNoLock("Exit with lock");
				break;
			case Opcodes.ATHROW:
				List<TryCatchBlockNode> handlers = analyzer.getHandlers(i);
				if (handlers == null || handlers.isEmpty()) {
					((LockFrame) frames[i]).assertNoLock("Exit with lock");
				}
				break;
			}
		}

		// Only instructions protected by a catch-all handler can hold locks:
		for (int i = 0; i < frames.length; i++) {
			AbstractInsnNode insn = mn.instructions.get(i);
			if (insn.getOpcode() > 0) {
				boolean catchAll = false;
				List<TryCatchBlockNode> handlers = analyzer.getHandlers(i);
				if (handlers != null) {
					for (TryCatchBlockNode node : handlers) {
						catchAll |= node.type == null;
					}
				}
				if (!catchAll) {
					((LockFrame) frames[i])
							.assertNoLock("No handlers for insn with lock");
				}
			}
		}

	}

	/**
	 * A Frame implementation that keeps track of the locking state. It is
	 * assumed that the monitor objects are stored in local variables.
	 */
	private static class LockFrame extends Frame<BasicValue> {

		Set<Integer> locks;

		public LockFrame(final int nLocals, final int nStack) {
			super(nLocals, nStack);
			locks = new HashSet<Integer>();
		}

		public LockFrame(Frame<? extends BasicValue> src) {
			super(src);
		}

		@Override
		public Frame<BasicValue> init(Frame<? extends BasicValue> src) {
			locks = new HashSet<Integer>(((LockFrame) src).locks);
			return super.init(src);
		}

		@Override
		public void execute(AbstractInsnNode insn,
				Interpreter<BasicValue> interpreter) throws AnalyzerException {
			super.execute(insn, interpreter);
			switch (insn.getOpcode()) {
			case Opcodes.MONITORENTER:
				// Lock is stored in a local variable:
				enter(((VarInsnNode) insn.getPrevious()).var);
				break;
			case Opcodes.MONITOREXIT:
				// Lock is stored in a local variable:
				exit(((VarInsnNode) insn.getPrevious()).var);
				break;
			}
		}

		void enter(int lock) {
			assertTrue("multiple ENTER for lock " + lock,
					locks.add(Integer.valueOf(lock)));
		}

		void exit(int lock) {
			assertTrue("invalid EXIT for lock " + lock,
					locks.remove(Integer.valueOf(lock)));
		}

		@Override
		public boolean merge(Frame<? extends BasicValue> frame,
				Interpreter<BasicValue> interpreter) throws AnalyzerException {
			this.locks.addAll(((LockFrame) frame).locks);
			return super.merge(frame, interpreter);
		}

		void assertNoLock(String message) {
			assertEquals(message, Collections.emptySet(), locks);

		}
	}

}
