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
package org.jacoco.core.analysis;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.data.IMethodStructureVisitor;
import org.jacoco.core.internal.flow.IProbeIdGenerator;
import org.jacoco.core.internal.flow.LabelFlowAnalyzer;
import org.jacoco.core.internal.flow.MethodProbesAdapter;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link MethodAnalyzer}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class MethodAnalyzerTest implements IProbeIdGenerator {

	private int nextProbeId;

	private boolean[] probes;

	private MethodNode method;

	private Map<Integer, LineInfo> lines;

	@Before
	public void setup() {
		nextProbeId = 0;
		lines = new HashMap<Integer, MethodAnalyzerTest.LineInfo>();
		method = new MethodNode();
		probes = new boolean[32];
	}

	public int nextId() {
		return nextProbeId++;
	}

	// === Scenario: linear Sequence without branches ===

	private void createLinearSequence() {
		method.visitLineNumber(1001, new Label());
		method.visitInsn(Opcodes.NOP);
		method.visitLineNumber(1002, new Label());
		method.visitInsn(Opcodes.RETURN);
	}

	@Test
	public void testLinearSequenceNotCovered1() {
		createLinearSequence();
		runMethodAnalzer();
		assertEquals(1, nextProbeId);

		assertLine(1001, 1, 0, 0, 0);
		assertLine(1002, 1, 0, 0, 0);
	}

	@Test
	public void testLinearSequenceNotCovered2() {
		createLinearSequence();
		probes = null;
		runMethodAnalzer();
		assertEquals(1, nextProbeId);

		assertLine(1001, 1, 0, 0, 0);
		assertLine(1002, 1, 0, 0, 0);
	}

	@Test
	public void testLinearSequenceCovered() {
		createLinearSequence();
		probes[0] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 1, 0, 0);
		assertLine(1002, 0, 1, 0, 0);
	}

	// === Scenario: simple if branch ===

	private void createIfBranch() {
		method.visitLineNumber(1001, new Label());
		method.visitVarInsn(Opcodes.ILOAD, 1);
		Label l1 = new Label();
		method.visitJumpInsn(Opcodes.IFEQ, l1);
		method.visitLineNumber(1002, new Label());
		method.visitLdcInsn("a");
		method.visitInsn(Opcodes.ARETURN);
		method.visitLabel(l1);
		method.visitLineNumber(1003, l1);
		method.visitLdcInsn("b");
		method.visitInsn(Opcodes.ARETURN);
	}

	@Test
	public void testIfBranchNotCovered() {
		createIfBranch();
		runMethodAnalzer();
		assertEquals(2, nextProbeId);

		assertLine(1001, 2, 0, 2, 0);
		assertLine(1002, 2, 0, 0, 0);
		assertLine(1003, 2, 0, 0, 0);
	}

	@Test
	public void testIfBranchCovered1() {
		createIfBranch();
		probes[0] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 1, 1);
		assertLine(1002, 0, 2, 0, 0);
		assertLine(1003, 2, 0, 0, 0);
	}

	@Test
	public void testIfBranchCovered2() {
		createIfBranch();
		probes[1] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 1, 1);
		assertLine(1002, 2, 0, 0, 0);
		assertLine(1003, 0, 2, 0, 0);
	}

	@Test
	public void testIfBranchCovered3() {
		createIfBranch();
		probes[0] = true;
		probes[1] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 0, 2);
		assertLine(1002, 0, 2, 0, 0);
		assertLine(1003, 0, 2, 0, 0);
	}

	// === Scenario: branch which merges back ===

	private void createIfBranchMerge() {
		method.visitLineNumber(1001, new Label());
		method.visitVarInsn(Opcodes.ILOAD, 1);
		Label l1 = new Label();
		method.visitJumpInsn(Opcodes.IFEQ, l1);
		method.visitLineNumber(1002, new Label());
		method.visitInsn(Opcodes.NOP);
		method.visitLabel(l1);
		method.visitLineNumber(1003, l1);
		method.visitInsn(Opcodes.RETURN);
	}

	@Test
	public void testIfBranchMergeNotCovered() {
		createIfBranchMerge();
		runMethodAnalzer();
		assertEquals(3, nextProbeId);

		assertLine(1001, 2, 0, 2, 0);
		assertLine(1002, 1, 0, 0, 0);
		assertLine(1003, 1, 0, 0, 0);
	}

	@Test
	public void testIfBranchMergeCovered1() {
		createIfBranchMerge();
		probes[0] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 1, 1);
		assertLine(1002, 1, 0, 0, 0);
		assertLine(1003, 1, 0, 0, 0);
	}

	@Test
	public void testIfBranchMergeCovered2() {
		createIfBranchMerge();
		probes[1] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 1, 1);
		assertLine(1002, 0, 1, 0, 0);
		assertLine(1003, 1, 0, 0, 0);
	}

	@Test
	public void testIfBranchMergeCovered3() {
		createIfBranchMerge();
		probes[0] = true;
		probes[1] = true;
		probes[2] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 2, 0, 2);
		assertLine(1002, 0, 1, 0, 0);
		assertLine(1003, 0, 1, 0, 0);
	}

	// === Scenario: branch which jump backwards ===

	private void createJumpBackwards() {
		method.visitLineNumber(1001, new Label());
		final Label l1 = new Label();
		method.visitJumpInsn(Opcodes.GOTO, l1);
		final Label l2 = new Label();
		method.visitLabel(l2);
		method.visitLineNumber(1002, l2);
		method.visitInsn(Opcodes.RETURN);
		method.visitLabel(l1);
		method.visitLineNumber(1003, l1);
		method.visitJumpInsn(Opcodes.GOTO, l2);
	}

	@Test
	public void testJumpBackwardsNotCovered() {
		createJumpBackwards();
		runMethodAnalzer();
		assertEquals(1, nextProbeId);

		assertLine(1001, 1, 0, 0, 0);
		assertLine(1002, 1, 0, 0, 0);
		assertLine(1003, 1, 0, 0, 0);
	}

	@Test
	public void testJumpBackwardsCovered() {
		createJumpBackwards();
		probes[0] = true;
		runMethodAnalzer();

		assertLine(1001, 0, 1, 0, 0);
		assertLine(1002, 0, 1, 0, 0);
		assertLine(1003, 0, 1, 0, 0);
	}

	// === Scenario: jump to first instruction ===

	private void createJumpToFirst() {
		final Label l1 = new Label();
		method.visitLabel(l1);
		method.visitLineNumber(1001, l1);
		method.visitVarInsn(Opcodes.ALOAD, 0);
		method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Foo", "test", "()Z");
		method.visitJumpInsn(Opcodes.IFEQ, l1);
		final Label l2 = new Label();
		method.visitLabel(l2);
		method.visitLineNumber(1002, l2);
		method.visitInsn(Opcodes.RETURN);
	}

	@Test
	public void testJumpToFirstNotCovered() {
		createJumpToFirst();
		runMethodAnalzer();
		assertEquals(2, nextProbeId);

		assertLine(1001, 3, 0, 2, 0);
		assertLine(1002, 1, 0, 0, 0);
	}

	@Test
	public void testJumpToFirstCovered1() {
		createJumpToFirst();
		probes[0] = true;
		runMethodAnalzer();
		assertEquals(2, nextProbeId);

		assertLine(1001, 0, 3, 1, 1);
		assertLine(1002, 1, 0, 0, 0);
	}

	@Test
	public void testJumpToFirstCovered2() {
		createJumpToFirst();
		probes[0] = true;
		probes[1] = true;
		runMethodAnalzer();
		assertEquals(2, nextProbeId);

		assertLine(1001, 0, 3, 0, 2);
		assertLine(1002, 0, 1, 0, 0);
	}

	// === Scenario: table switch ===

	private void createTableSwitch() {
		method.visitLineNumber(1001, new Label());
		method.visitVarInsn(Opcodes.ILOAD, 1);
		Label l1 = new Label();
		Label l2 = new Label();
		Label l3 = new Label();
		method.visitTableSwitchInsn(1, 3, l3, new Label[] { l1, l2, l1 });
		method.visitLabel(l1);
		method.visitLineNumber(1002, l1);
		method.visitIntInsn(Opcodes.BIPUSH, 11);
		method.visitVarInsn(Opcodes.ISTORE, 2);
		method.visitLineNumber(1003, new Label());
		Label l5 = new Label();
		method.visitJumpInsn(Opcodes.GOTO, l5);
		method.visitLabel(l2);
		method.visitLineNumber(1004, l2);
		method.visitIntInsn(Opcodes.BIPUSH, 22);
		method.visitVarInsn(Opcodes.ISTORE, 2);
		method.visitLineNumber(1005, new Label());
		method.visitJumpInsn(Opcodes.GOTO, l5);
		method.visitLabel(l3);
		method.visitLineNumber(1006, l3);
		method.visitInsn(Opcodes.ICONST_0);
		method.visitVarInsn(Opcodes.ISTORE, 2);
		method.visitLabel(l5);
		method.visitLineNumber(1007, l5);
		method.visitVarInsn(Opcodes.ILOAD, 2);
		method.visitInsn(Opcodes.IRETURN);
	}

	@Test
	public void testTableSwitchNotCovered() {
		createTableSwitch();
		runMethodAnalzer();
		assertEquals(4, nextProbeId);

		assertLine(1001, 2, 0, 3, 0);
		assertLine(1002, 2, 0, 0, 0);
		assertLine(1003, 1, 0, 0, 0);
		assertLine(1004, 2, 0, 0, 0);
		assertLine(1005, 1, 0, 0, 0);
		assertLine(1006, 2, 0, 0, 0);
		assertLine(1007, 2, 0, 0, 0);
	}

	@Test
	public void testTableSwitchCovered1() {
		createTableSwitch();
		probes[0] = true;
		probes[3] = true;
		runMethodAnalzer();
		assertEquals(4, nextProbeId);

		assertLine(1001, 0, 2, 2, 1);
		assertLine(1002, 0, 2, 0, 0);
		assertLine(1003, 0, 1, 0, 0);
		assertLine(1004, 2, 0, 0, 0);
		assertLine(1005, 1, 0, 0, 0);
		assertLine(1006, 2, 0, 0, 0);
		assertLine(1007, 0, 2, 0, 0);
	}

	@Test
	public void testTableSwitchCovered2() {
		createTableSwitch();
		probes[2] = true;
		probes[3] = true;
		runMethodAnalzer();
		assertEquals(4, nextProbeId);

		assertLine(1001, 0, 2, 2, 1);
		assertLine(1002, 2, 0, 0, 0);
		assertLine(1003, 1, 0, 0, 0);
		assertLine(1004, 2, 0, 0, 0);
		assertLine(1005, 1, 0, 0, 0);
		assertLine(1006, 0, 2, 0, 0);
		assertLine(1007, 0, 2, 0, 0);
	}

	@Test
	public void testTableSwitchCovered3() {
		createTableSwitch();
		probes[0] = true;
		probes[1] = true;
		probes[2] = true;
		probes[3] = true;
		runMethodAnalzer();
		assertEquals(4, nextProbeId);

		assertLine(1001, 0, 2, 0, 3);
		assertLine(1002, 0, 2, 0, 0);
		assertLine(1003, 0, 1, 0, 0);
		assertLine(1004, 0, 2, 0, 0);
		assertLine(1005, 0, 1, 0, 0);
		assertLine(1006, 0, 2, 0, 0);
		assertLine(1007, 0, 2, 0, 0);
	}

	// === Scenario: table switch with merge ===

	private void createTableSwitchMerge() {
		method.visitLineNumber(1001, new Label());
		method.visitInsn(Opcodes.ICONST_0);
		method.visitVarInsn(Opcodes.ISTORE, 2);
		method.visitLineNumber(1002, new Label());
		method.visitVarInsn(Opcodes.ILOAD, 1);
		Label l2 = new Label();
		Label l3 = new Label();
		Label l4 = new Label();
		method.visitTableSwitchInsn(1, 3, l4, new Label[] { l2, l3, l2 });
		method.visitLabel(l2);
		method.visitLineNumber(1003, l2);
		method.visitIincInsn(2, 1);
		method.visitLabel(l3);
		method.visitLineNumber(1004, l3);
		method.visitIincInsn(2, 1);
		method.visitLabel(l4);
		method.visitLineNumber(1005, l4);
		method.visitVarInsn(Opcodes.ILOAD, 2);
		method.visitInsn(Opcodes.IRETURN);
	}

	@Test
	public void testTableSwitchMergeNotCovered() {
		createTableSwitchMerge();
		runMethodAnalzer();
		assertEquals(5, nextProbeId);

		assertLine(1001, 2, 0, 0, 0);
		assertLine(1002, 2, 0, 3, 0);
		assertLine(1003, 1, 0, 0, 0);
		assertLine(1004, 1, 0, 0, 0);
		assertLine(1005, 2, 0, 0, 0);
	}

	@Test
	public void testTableSwitchMergeNotCovered1() {
		createTableSwitchMerge();
		probes[0] = true;
		probes[4] = true;
		runMethodAnalzer();
		assertEquals(5, nextProbeId);

		assertLine(1001, 0, 2, 0, 0);
		assertLine(1002, 0, 2, 2, 1);
		assertLine(1003, 1, 0, 0, 0);
		assertLine(1004, 1, 0, 0, 0);
		assertLine(1005, 0, 2, 0, 0);
	}

	@Test
	public void testTableSwitchMergeNotCovered2() {
		createTableSwitchMerge();
		probes[1] = true;
		probes[3] = true;
		probes[4] = true;
		runMethodAnalzer();
		assertEquals(5, nextProbeId);

		assertLine(1001, 0, 2, 0, 0);
		assertLine(1002, 0, 2, 2, 1);
		assertLine(1003, 1, 0, 0, 0);
		assertLine(1004, 0, 1, 0, 0);
		assertLine(1005, 0, 2, 0, 0);
	}

	@Test
	public void testTableSwitchMergeNotCovered3() {
		createTableSwitchMerge();
		probes[2] = true;
		probes[3] = true;
		probes[4] = true;
		runMethodAnalzer();
		assertEquals(5, nextProbeId);

		assertLine(1001, 0, 2, 0, 0);
		assertLine(1002, 0, 2, 2, 1);
		assertLine(1003, 0, 1, 0, 0);
		assertLine(1004, 0, 1, 0, 0);
		assertLine(1005, 0, 2, 0, 0);
	}

	@Test
	public void testTableSwitchMergeNotCovered4() {
		createTableSwitchMerge();
		probes[0] = true;
		probes[1] = true;
		probes[2] = true;
		probes[3] = true;
		probes[4] = true;
		runMethodAnalzer();
		assertEquals(5, nextProbeId);

		assertLine(1001, 0, 2, 0, 0);
		assertLine(1002, 0, 2, 0, 3);
		assertLine(1003, 0, 1, 0, 0);
		assertLine(1004, 0, 1, 0, 0);
		assertLine(1005, 0, 2, 0, 0);
	}

	private void runMethodAnalzer() {
		method.accept(new LabelFlowAnalyzer());
		final boolean[] endCalled = new boolean[] { false };
		final IMethodStructureVisitor output = new IMethodStructureVisitor() {
			public void visitInsn(boolean covered, int line) {
				getLine(line).addInsn(covered);
			}

			public void visitBranches(int total, int covered, int line) {
				getLine(line).addBranches(total - covered, covered);
			}

			public void visitEnd() {
				endCalled[0] = true;
			}
		};
		final MethodAnalyzer analyzer = new MethodAnalyzer(probes, output);
		method.accept(new MethodProbesAdapter(analyzer, this));
		assertTrue(endCalled[0]);
	}

	private LineInfo getLine(int nr) {
		final Integer key = Integer.valueOf(nr);
		LineInfo info = lines.get(key);
		if (info == null) {
			lines.put(key, info = new LineInfo());
		}
		return info;
	}

	private void assertLine(int nr, int insnMissed, int insnCovered,
			int branchesMissed, int branchesCovered) {
		assertEquals("Line " + nr, new LineInfo(insnMissed, insnCovered,
				branchesMissed, branchesCovered), getLine(nr));
	}

	private static class LineInfo {

		private int insnMissed;

		private int insnCovered;

		private int branchesMissed;

		private int branchesCovered;

		LineInfo(int insnMissed, int insnCovered, int branchesMissed,
				int branchesCovered) {
			this.insnMissed = insnMissed;
			this.insnCovered = insnCovered;
			this.branchesMissed = branchesMissed;
			this.branchesCovered = branchesCovered;
		}

		LineInfo() {
			this(0, 0, 0, 0);
		}

		void addInsn(boolean covered) {
			if (covered) {
				insnCovered++;
			} else {
				insnMissed++;
			}
		}

		public void addBranches(int missed, int covered) {
			branchesMissed += missed;
			branchesCovered += covered;
		}

		@Override
		public int hashCode() {
			return insnMissed ^ insnCovered ^ branchesMissed ^ branchesCovered;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof LineInfo)) {
				return false;
			}
			LineInfo that = (LineInfo) obj;
			return this.insnMissed == that.insnMissed
					&& this.insnCovered == that.insnCovered
					&& this.branchesMissed == that.branchesMissed
					&& this.branchesCovered == that.branchesCovered;
		}

		@Override
		public String toString() {
			return "[insn: " + insnMissed + "/" + insnCovered + ", branches: "
					+ branchesMissed + "," + branchesCovered + "]";
		}

	}

}
