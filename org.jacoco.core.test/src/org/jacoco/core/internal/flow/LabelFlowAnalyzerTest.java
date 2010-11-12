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
package org.jacoco.core.internal.flow;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.RETURN;

import org.junit.Test;
import org.objectweb.asm.Label;

/**
 * Unit tests for {@link LabelFlowAnalyzer}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class LabelFlowAnalyzerTest extends SuccessorAnalyzerTestBase {

	private LabelFlowAnalyzer analyzer;

	@Override
	protected SuccessorAnalyzer createAnalyzer() {
		return analyzer = new LabelFlowAnalyzer();
	}

	@Test
	public void testFlowScenario01() {
		assertFalse(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario02() {
		analyzer.visitJumpInsn(GOTO, label);
		assertFalse(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario03() {
		analyzer.visitInsn(RETURN);
		analyzer.visitLabel(label);
		assertFalse(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario04() {
		analyzer.visitLabel(label);
		assertFalse(LabelInfo.isMultiTarget(label));
		assertTrue(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario05() {
		analyzer.visitLabel(label);
		analyzer.visitJumpInsn(GOTO, label);
		assertTrue(LabelInfo.isMultiTarget(label));
		assertTrue(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario06() {
		analyzer.visitJumpInsn(IFEQ, label);
		analyzer.visitLabel(label);
		assertTrue(LabelInfo.isMultiTarget(label));
		assertTrue(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario07() {
		analyzer.visitJumpInsn(IFEQ, label);
		analyzer.visitJumpInsn(GOTO, label);
		assertTrue(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario08() {
		analyzer.visitJumpInsn(IFEQ, label);
		analyzer.visitJumpInsn(IFGT, label);
		analyzer.visitLabel(label);
		assertTrue(LabelInfo.isMultiTarget(label));
		assertTrue(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario09() {
		analyzer.visitLabel(label);
		analyzer.visitLabel(label);
		assertFalse(LabelInfo.isMultiTarget(label));
		assertTrue(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario10() {
		analyzer.visitTryCatchBlock(new Label(), new Label(), label,
				"java/lang/Exception");
		analyzer.visitJumpInsn(GOTO, label);
		assertTrue(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario11() {
		// Even if the same label is referenced multiple times but from the same
		// source instruction this is only counted as one target.
		analyzer.visitLookupSwitchInsn(label, new int[] { 0, 1 }, new Label[] {
				label, label });
		assertFalse(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

	@Test
	public void testFlowScenario12() {
		// Even if the same label is referenced multiple times but from the same
		// source instruction this is only counted as one target.
		analyzer.visitTableSwitchInsn(0, 1, label, new Label[] { label, label });
		assertFalse(LabelInfo.isMultiTarget(label));
		assertFalse(LabelInfo.isSuccessor(label));
	}

}
