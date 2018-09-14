/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.core.internal.analysis.filter.IFilter;
import org.jacoco.core.internal.analysis.filter.IFilterContext;
import org.jacoco.core.internal.analysis.filter.IFilterOutput;
import org.jacoco.core.internal.flow.IFrame;
import org.jacoco.core.internal.flow.Instruction;
import org.jacoco.core.internal.flow.LabelInfo;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * A {@link MethodProbesVisitor} that analyzes which statements and branches of
 * a method have been executed based on given probe data.
 */
public class MethodAnalyzer extends MethodProbesVisitor
		implements IFilterOutput {

	private final boolean[] probes;

	private final IFilter filter;

	private final IFilterContext filterContext;

	private final MethodCoverageImpl coverage;

	private int currentLine = ISourceNode.UNKNOWN_LINE;

	private int firstLine = ISourceNode.UNKNOWN_LINE;

	private int lastLine = ISourceNode.UNKNOWN_LINE;

	// Due to ASM issue #315745 there can be more than one label per instruction
	private final List<Label> currentLabel = new ArrayList<Label>(2);

	/** List of all analyzed instructions */
	private final List<Instruction> instructions = new ArrayList<Instruction>();

	/** List of all predecessors of covered probes */
	private final List<CoveredProbe> coveredProbes = new ArrayList<CoveredProbe>();

	/** List of all jumps encountered */
	private final List<Jump> jumps = new ArrayList<Jump>();

	/** Last instruction in byte code sequence */
	private Instruction lastInsn;

	/**
	 * New Method analyzer for the given probe data.
	 *
	 * @param name
	 *            method name
	 * @param desc
	 *            method descriptor
	 * @param signature
	 *            optional parameterized signature
	 * @param probes
	 *            recorded probe date of the containing class or
	 *            <code>null</code> if the class is not executed at all
	 * @param filter
	 *            filter which should be applied
	 * @param filterContext
	 *            class context information for the filter
	 */
	MethodAnalyzer(final String name, final String desc, final String signature,
			final boolean[] probes, final IFilter filter,
			final IFilterContext filterContext) {
		super();
		this.probes = probes;
		this.filter = filter;
		this.filterContext = filterContext;
		this.coverage = new MethodCoverageImpl(name, desc, signature);
	}

	/**
	 * Returns the coverage data for this method after this visitor has been
	 * processed.
	 * 
	 * @return coverage data for this method
	 */
	public IMethodCoverage getCoverage() {
		return coverage;
	}

	/**
	 * {@link MethodNode#accept(MethodVisitor)}
	 */
	@Override
	public void accept(final MethodNode methodNode,
			final MethodVisitor methodVisitor) {
		filter.filter(methodNode, filterContext, this);

		methodVisitor.visitCode();
		for (final TryCatchBlockNode n : methodNode.tryCatchBlocks) {
			n.accept(methodVisitor);
		}
		currentNode = methodNode.instructions.getFirst();
		while (currentNode != null) {
			currentNode.accept(methodVisitor);
			currentNode = currentNode.getNext();
		}
		methodVisitor.visitEnd();
	}

	private final Set<AbstractInsnNode> ignored = new HashSet<AbstractInsnNode>();

	/**
	 * Instructions that should be merged form disjoint sets. Coverage
	 * information from instructions of one set will be merged into
	 * representative instruction of set.
	 * 
	 * Each such set is represented as a singly linked list: each element except
	 * one references another element from the same set, element without
	 * reference - is a representative of this set.
	 * 
	 * This map stores reference (value) for elements of sets (key).
	 */
	private final Map<AbstractInsnNode, AbstractInsnNode> merged = new HashMap<AbstractInsnNode, AbstractInsnNode>();

	private final Map<AbstractInsnNode, Instruction> nodeToInstruction = new HashMap<AbstractInsnNode, Instruction>();

	private AbstractInsnNode currentNode;

	public void ignore(final AbstractInsnNode fromInclusive,
			final AbstractInsnNode toInclusive) {
		for (AbstractInsnNode i = fromInclusive; i != toInclusive; i = i
				.getNext()) {
			ignored.add(i);
		}
		ignored.add(toInclusive);
	}

	private AbstractInsnNode findRepresentative(AbstractInsnNode i) {
		AbstractInsnNode r = merged.get(i);
		while (r != null) {
			i = r;
			r = merged.get(i);
		}
		return i;
	}

	public void merge(AbstractInsnNode i1, AbstractInsnNode i2) {
		i1 = findRepresentative(i1);
		i2 = findRepresentative(i2);
		if (i1 != i2) {
			merged.put(i2, i1);
		}
	}

	private final Map<AbstractInsnNode, Set<AbstractInsnNode>> replacements = new HashMap<AbstractInsnNode, Set<AbstractInsnNode>>();

	public void replaceBranches(final AbstractInsnNode source,
			final Set<AbstractInsnNode> newTargets) {
		replacements.put(source, newTargets);
	}

	@Override
	public void visitLabel(final Label label) {
		currentLabel.add(label);
		if (!LabelInfo.isSuccessor(label)) {
			lastInsn = null;
		}
	}

	@Override
	public void visitLineNumber(final int line, final Label start) {
		currentLine = line;
		if (firstLine > line || lastLine == ISourceNode.UNKNOWN_LINE) {
			firstLine = line;
		}
		if (lastLine < line) {
			lastLine = line;
		}
	}

	private void visitInsn() {
		final Instruction insn = new Instruction(currentNode, currentLine);
		nodeToInstruction.put(currentNode, insn);
		instructions.add(insn);
		if (lastInsn != null) {
			insn.setPredecessor(lastInsn, 0);
		}
		final int labelCount = currentLabel.size();
		if (labelCount > 0) {
			for (int i = labelCount; --i >= 0;) {
				LabelInfo.setInstruction(currentLabel.get(i), insn);
			}
			currentLabel.clear();
		}
		lastInsn = insn;
	}

	@Override
	public void visitInsn(final int opcode) {
		visitInsn();
	}

	@Override
	public void visitIntInsn(final int opcode, final int operand) {
		visitInsn();
	}

	@Override
	public void visitVarInsn(final int opcode, final int var) {
		visitInsn();
	}

	@Override
	public void visitTypeInsn(final int opcode, final String type) {
		visitInsn();
	}

	@Override
	public void visitFieldInsn(final int opcode, final String owner,
			final String name, final String desc) {
		visitInsn();
	}

	@Override
	public void visitMethodInsn(final int opcode, final String owner,
			final String name, final String desc, final boolean itf) {
		visitInsn();
	}

	@Override
	public void visitInvokeDynamicInsn(final String name, final String desc,
			final Handle bsm, final Object... bsmArgs) {
		visitInsn();
	}

	@Override
	public void visitJumpInsn(final int opcode, final Label label) {
		visitInsn();
		jumps.add(new Jump(lastInsn, label, 1));
	}

	@Override
	public void visitLdcInsn(final Object cst) {
		visitInsn();
	}

	@Override
	public void visitIincInsn(final int var, final int increment) {
		visitInsn();
	}

	@Override
	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label... labels) {
		visitSwitchInsn(dflt, labels);
	}

	@Override
	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		visitSwitchInsn(dflt, labels);
	}

	private void visitSwitchInsn(final Label dflt, final Label[] labels) {
		visitInsn();
		LabelInfo.resetDone(labels);
		int branch = 0;
		jumps.add(new Jump(lastInsn, dflt, branch));
		LabelInfo.setDone(dflt);
		for (final Label l : labels) {
			if (!LabelInfo.isDone(l)) {
				branch++;
				jumps.add(new Jump(lastInsn, l, branch));
				LabelInfo.setDone(l);
			}
		}
	}

	@Override
	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		visitInsn();
	}

	@Override
	public void visitProbe(final int probeId) {
		addProbe(probeId, 0);
		lastInsn = null;
	}

	@Override
	public void visitJumpInsnWithProbe(final int opcode, final Label label,
			final int probeId, final IFrame frame) {
		visitInsn();
		addProbe(probeId, 1);
	}

	@Override
	public void visitInsnWithProbe(final int opcode, final int probeId) {
		visitInsn();
		addProbe(probeId, 0);
	}

	@Override
	public void visitTableSwitchInsnWithProbes(final int min, final int max,
			final Label dflt, final Label[] labels, final IFrame frame) {
		visitSwitchInsnWithProbes(dflt, labels);
	}

	@Override
	public void visitLookupSwitchInsnWithProbes(final Label dflt,
			final int[] keys, final Label[] labels, final IFrame frame) {
		visitSwitchInsnWithProbes(dflt, labels);
	}

	private void visitSwitchInsnWithProbes(final Label dflt,
			final Label[] labels) {
		visitInsn();
		LabelInfo.resetDone(dflt);
		LabelInfo.resetDone(labels);
		int branch = 0;
		visitSwitchTarget(dflt, branch);
		for (final Label l : labels) {
			branch++;
			visitSwitchTarget(l, branch);
		}
	}

	private void visitSwitchTarget(final Label label, final int branch) {
		final int id = LabelInfo.getProbeId(label);
		if (!LabelInfo.isDone(label)) {
			if (id == LabelInfo.NO_PROBE) {
				jumps.add(new Jump(lastInsn, label, branch));
			} else {
				addProbe(id, branch);
			}
			LabelInfo.setDone(label);
		}
	}

	@Override
	public void visitEnd() {
		// Wire jumps:
		for (final Jump j : jumps) {
			LabelInfo.getInstruction(j.target).setPredecessor(j.source,
					j.branch);
		}
		// Propagate probe values:
		for (final CoveredProbe p : coveredProbes) {
			p.instruction.setCovered(p.branch);
		}

		// Merge into representative instruction:
		for (final Instruction i : instructions) {
			final AbstractInsnNode m = i.getNode();
			final AbstractInsnNode r = findRepresentative(m);
			if (r != m) {
				ignored.add(m);
				nodeToInstruction.get(r).merge(i);
			}
		}

		// Merge from representative instruction, because result of merge might
		// be used to compute coverage of instructions with replaced branches:
		for (final Instruction i : instructions) {
			final AbstractInsnNode m = i.getNode();
			final AbstractInsnNode r = findRepresentative(m);
			if (r != m) {
				i.merge(nodeToInstruction.get(r));
			}
		}

		// Report result:
		coverage.ensureCapacity(firstLine, lastLine);
		for (final Instruction i : instructions) {
			if (ignored.contains(i.getNode())) {
				continue;
			}

			final int total;
			final int covered;
			final Set<AbstractInsnNode> r = replacements.get(i.getNode());
			if (r != null) {
				int cb = 0;
				for (AbstractInsnNode b : r) {
					if (nodeToInstruction.get(b).getCoveredBranches() > 0) {
						cb++;
					}
				}
				total = r.size();
				covered = cb;
			} else {
				total = i.getBranches();
				covered = i.getCoveredBranches();
			}

			final ICounter instrCounter = covered == 0 ? CounterImpl.COUNTER_1_0
					: CounterImpl.COUNTER_0_1;
			final ICounter branchCounter = total > 1
					? CounterImpl.getInstance(total - covered, covered)
					: CounterImpl.COUNTER_0_0;
			coverage.increment(instrCounter, branchCounter, i.getLine());
		}
		coverage.incrementMethodCounter();
	}

	private void addProbe(final int probeId, final int branch) {
		lastInsn.addBranch();
		if (probes != null && probes[probeId]) {
			coveredProbes.add(new CoveredProbe(lastInsn, branch));
		}
	}

	private static class CoveredProbe {
		final Instruction instruction;
		final int branch;

		private CoveredProbe(final Instruction instruction, final int branch) {
			this.instruction = instruction;
			this.branch = branch;
		}
	}

	private static class Jump {

		final Instruction source;
		final Label target;
		final int branch;

		Jump(final Instruction source, final Label target, final int branch) {
			this.source = source;
			this.target = target;
			this.branch = branch;
		}
	}

}
