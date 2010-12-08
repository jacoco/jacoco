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

import static junit.framework.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.internal.flow.MethodAnalyzer.Output;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
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

	private MethodVisitor visitor;

	private Map<Integer, LineInfo> lines;

	@Before
	public void setup() {
		nextProbeId = 0;
		lines = new HashMap<Integer, MethodAnalyzerTest.LineInfo>();
		method = new MethodNode();
		final MethodAnalyzer.Output output = new Output() {
			public void visitInsn(boolean covered, int line) {
				getLine(line).addInsn(covered);
			}

			public void visitBranch(boolean covered, int line) {
				getLine(line).addBranch(covered);
			}
		};
		probes = new boolean[32];
		final MethodAnalyzer analyzer = new MethodAnalyzer(probes, output);
		visitor = new MethodProbesAdapter(analyzer, this);
	}

	public int nextId() {
		return nextProbeId++;
	}

	@Test
	public void testSimpleFlowNotCovered() {
		method.visitLineNumber(5, new Label());
		method.visitInsn(Opcodes.RETURN);

		runMethodAnalzer();

		assertLine(5, 1, 0, 0, 0);
	}

	@Test
	public void testSimpleFlowCovered() {
		method.visitLineNumber(5, new Label());
		method.visitInsn(Opcodes.RETURN);

		probes[0] = true;
		runMethodAnalzer();

		assertLine(5, 0, 1, 0, 0);
	}

	private void runMethodAnalzer() {
		method.accept(new LabelFlowAnalyzer());
		method.accept(visitor);
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

		void addBranch(boolean covered) {
			if (covered) {
				branchesCovered++;
			} else {
				branchesMissed++;
			}
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
