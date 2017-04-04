/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class TryWithResourcesEcjFilterTest implements IFilterOutput {

	private final TryWithResourcesEcjFilter filter = new TryWithResourcesEcjFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	/**
	 * ECJ for
	 *
	 * <pre>
	 *     try (r0 = ...; r1 = ...; r2= ...) {
	 *         ...
	 *     } finally (...) {
	 *         ...
	 *     }
	 * </pre>
	 */
	@Test
	public void ecj() {
		final Range range0 = new Range();
		final Range range1 = new Range();

		final Label handler = new Label();
		m.visitTryCatchBlock(handler, handler, handler, null);

		// primaryExc = null
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		// suppressedExc = null
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitVarInsn(Opcodes.ASTORE, 2);

		// body
		m.visitInsn(Opcodes.NOP);

		final Label l4 = new Label();
		final Label l7 = new Label();
		final Label end = new Label();
		{ // nextIsEcjClose("r0")
			m.visitVarInsn(Opcodes.ALOAD, 5);
			range0.fromInclusive = m.instructions.getLast();
			m.visitJumpInsn(Opcodes.IFNULL, l4);
			m.visitVarInsn(Opcodes.ALOAD, 5);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun2$Resource", "close",
					"()V", false);
		}
		m.visitJumpInsn(Opcodes.GOTO, l4);
		range0.toInclusive = m.instructions.getLast();
		// catch (any primaryExc)
		m.visitLabel(handler);
		range1.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 1);
		{ // nextIsEcjCloseAndThrow("r0")
			m.visitVarInsn(Opcodes.ALOAD, 5);
			Label l11 = new Label();
			m.visitJumpInsn(Opcodes.IFNULL, l11);
			m.visitVarInsn(Opcodes.ALOAD, 5);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun2$Resource", "close",
					"()V", false);
			m.visitLabel(l11);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitInsn(Opcodes.ATHROW);
		}
		m.visitLabel(l4);
		{ // nextIsEcjClose("r1")
			m.visitVarInsn(Opcodes.ALOAD, 4);
			m.visitJumpInsn(Opcodes.IFNULL, l7);
			m.visitVarInsn(Opcodes.ALOAD, 4);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun2$Resource", "close",
					"()V", false);
		}
		m.visitJumpInsn(Opcodes.GOTO, l7);
		{ // nextIsEcjSuppress
			m.visitVarInsn(Opcodes.ASTORE, 2);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			final Label suppressStart = new Label();
			m.visitJumpInsn(Opcodes.IFNONNULL, suppressStart);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitVarInsn(Opcodes.ASTORE, 1);
			final Label suppressEnd = new Label();
			m.visitJumpInsn(Opcodes.GOTO, suppressEnd);
			m.visitLabel(suppressStart);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitJumpInsn(Opcodes.IF_ACMPEQ, suppressEnd);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
					"addSuppressed", "(Ljava/lang/Throwable;)V", false);
			m.visitLabel(suppressEnd);
		}
		{ // nextIsEcjCloseAndThrow("r1")
			m.visitVarInsn(Opcodes.ALOAD, 4);
			final Label l14 = new Label();
			m.visitJumpInsn(Opcodes.IFNULL, l14);
			m.visitVarInsn(Opcodes.ALOAD, 4);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun2$Resource", "close",
					"()V", false);
			m.visitLabel(l14);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitInsn(Opcodes.ATHROW);
		}
		m.visitLabel(l7);
		{ // nextIsEcjClose("r2")
			m.visitVarInsn(Opcodes.ALOAD, 3);
			m.visitJumpInsn(Opcodes.IFNULL, end);
			m.visitVarInsn(Opcodes.ALOAD, 3);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun2$Resource", "close",
					"()V", false);
			m.visitJumpInsn(Opcodes.GOTO, end);
		}
		{ // nextIsEcjSuppress
			m.visitVarInsn(Opcodes.ASTORE, 2);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			final Label suppressStart = new Label();
			m.visitJumpInsn(Opcodes.IFNONNULL, suppressStart);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitVarInsn(Opcodes.ASTORE, 1);
			final Label suppressEnd = new Label();
			m.visitJumpInsn(Opcodes.GOTO, suppressEnd);
			m.visitLabel(suppressStart);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitJumpInsn(Opcodes.IF_ACMPEQ, suppressEnd);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
					"addSuppressed", "(Ljava/lang/Throwable;)V", false);
			m.visitLabel(suppressEnd);
		}
		{ // nextIsEcjCloseAndThrow("r2")
			m.visitVarInsn(Opcodes.ALOAD, 3);
			final Label l18 = new Label();
			m.visitJumpInsn(Opcodes.IFNULL, l18);
			m.visitVarInsn(Opcodes.ALOAD, 3);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun2$Resource", "close",
					"()V", false);
			m.visitLabel(l18);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitInsn(Opcodes.ATHROW);
		}
		{ // nextIsEcjSuppress
			m.visitVarInsn(Opcodes.ASTORE, 2);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			final Label suppressStart = new Label();
			m.visitJumpInsn(Opcodes.IFNONNULL, suppressStart);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitVarInsn(Opcodes.ASTORE, 1);
			final Label suppressEnd = new Label();
			m.visitJumpInsn(Opcodes.GOTO, suppressEnd);
			m.visitLabel(suppressStart);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitJumpInsn(Opcodes.IF_ACMPEQ, suppressEnd);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
					"addSuppressed", "(Ljava/lang/Throwable;)V", false);
			m.visitLabel(suppressEnd);
		}
		// throw primaryExc
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ATHROW);
		range1.toInclusive = m.instructions.getLast();

		// additional handlers
		m.visitInsn(Opcodes.NOP);

		filter.filter("Foo", "java/lang/Object", m, this);

		assertEquals(2, from.size());

		assertEquals(range0.fromInclusive, from.get(0));
		assertEquals(range0.toInclusive, to.get(0));

		assertEquals(range1.fromInclusive, from.get(1));
		assertEquals(range1.toInclusive, to.get(1));
	}

	/**
	 * ECJ for
	 * 
	 * <pre>
	 *     try (r1 = ...; r2 = ...; r3 = ...) {
	 *         return ...
	 *     } finally {
	 *         ...
	 *     }
	 * </pre>
	 */
	@Test
	public void ecj_noFlowOut() {
		final Range range0 = new Range();
		final Range range1 = new Range();

		final Label handler = new Label();
		m.visitTryCatchBlock(handler, handler, handler, null);

		// primaryExc = null
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitVarInsn(Opcodes.ASTORE, 1);
		// suppressedExc = null
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitVarInsn(Opcodes.ASTORE, 2);

		// body
		m.visitInsn(Opcodes.NOP);

		{ // nextIsEcjClose("r0")
			final Label label = new Label();
			m.visitVarInsn(Opcodes.ALOAD, 5);
			range0.fromInclusive = m.instructions.getLast();
			m.visitJumpInsn(Opcodes.IFNULL, label);
			m.visitVarInsn(Opcodes.ALOAD, 5);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun$Resource", "close",
					"()V", false);
			m.visitLabel(label);
		}
		{ // nextIsEcjClose("r1")
			final Label label = new Label();
			m.visitVarInsn(Opcodes.ALOAD, 4);
			m.visitJumpInsn(Opcodes.IFNULL, label);
			m.visitVarInsn(Opcodes.ALOAD, 4);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun$Resource", "close",
					"()V", false);
			m.visitLabel(label);
		}
		{ // nextIsEcjClose("r2")
			final Label label = new Label();
			m.visitVarInsn(Opcodes.ALOAD, 3);
			m.visitJumpInsn(Opcodes.IFNULL, label);
			m.visitVarInsn(Opcodes.ALOAD, 3);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun$Resource", "close",
					"()V", false);
			range0.toInclusive = m.instructions.getLast();
			m.visitLabel(label);
		}

		// finally
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.ARETURN);

		// catch (any primaryExc)
		m.visitLabel(handler);
		range1.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 1);
		{ // nextIsEcjCloseAndThrow("r0")
			m.visitVarInsn(Opcodes.ALOAD, 5);
			final Label throwLabel = new Label();
			m.visitJumpInsn(Opcodes.IFNULL, throwLabel);
			m.visitVarInsn(Opcodes.ALOAD, 5);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun$Resource", "close",
					"()V", false);
			m.visitLabel(throwLabel);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitInsn(Opcodes.ATHROW);
		}
		{ // nextIsEcjSuppress
			m.visitVarInsn(Opcodes.ASTORE, 2);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			final Label suppressStart = new Label();
			m.visitJumpInsn(Opcodes.IFNONNULL, suppressStart);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitVarInsn(Opcodes.ASTORE, 1);
			final Label suppressEnd = new Label();
			m.visitJumpInsn(Opcodes.GOTO, suppressEnd);
			m.visitLabel(suppressStart);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitJumpInsn(Opcodes.IF_ACMPEQ, suppressEnd);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
					"addSuppressed", "(Ljava/lang/Throwable;)V", false);
			m.visitLabel(suppressEnd);
		}
		{ // nextIsEcjCloseAndThrow("r1")
			m.visitVarInsn(Opcodes.ALOAD, 4);
			final Label throwLabel = new Label();
			m.visitJumpInsn(Opcodes.IFNULL, throwLabel);
			m.visitVarInsn(Opcodes.ALOAD, 4);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun$Resource", "close",
					"()V", false);
			m.visitLabel(throwLabel);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitInsn(Opcodes.ATHROW);
		}
		{ // nextIsEcjSuppress
			m.visitVarInsn(Opcodes.ASTORE, 2);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			final Label suppressStart = new Label();
			m.visitJumpInsn(Opcodes.IFNONNULL, suppressStart);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitVarInsn(Opcodes.ASTORE, 1);
			final Label suppressEnd = new Label();
			m.visitJumpInsn(Opcodes.GOTO, suppressEnd);
			m.visitLabel(suppressStart);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitJumpInsn(Opcodes.IF_ACMPEQ, suppressEnd);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
					"addSuppressed", "(Ljava/lang/Throwable;)V", false);
			m.visitLabel(suppressEnd);
		}
		{ // nextIsEcjCloseAndThrow("r2")
			m.visitVarInsn(Opcodes.ALOAD, 3);
			final Label throwLabel = new Label();
			m.visitJumpInsn(Opcodes.IFNULL, throwLabel);
			m.visitVarInsn(Opcodes.ALOAD, 3);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun$Resource", "close",
					"()V", false);
			m.visitLabel(throwLabel);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitInsn(Opcodes.ATHROW);
		}
		{ // nextIsEcjSuppress
			m.visitVarInsn(Opcodes.ASTORE, 2);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			final Label suppressStart = new Label();
			m.visitJumpInsn(Opcodes.IFNONNULL, suppressStart);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitVarInsn(Opcodes.ASTORE, 1);
			final Label suppressEnd = new Label();
			m.visitJumpInsn(Opcodes.GOTO, suppressEnd);
			m.visitLabel(suppressStart);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitJumpInsn(Opcodes.IF_ACMPEQ, suppressEnd);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
					"addSuppressed", "(Ljava/lang/Throwable;)V", false);
			m.visitLabel(suppressEnd);
		}
		// throw primaryExc
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitInsn(Opcodes.ATHROW);
		range1.toInclusive = m.instructions.getLast();

		// additional handlers
		m.visitInsn(Opcodes.NOP);

		filter.filter("Foo", "java/lang/Object", m, this);

		assertEquals(2, from.size());

		assertEquals(range0.fromInclusive, from.get(0));
		assertEquals(range0.toInclusive, to.get(0));

		assertEquals(range1.fromInclusive, from.get(1));
		assertEquals(range1.toInclusive, to.get(1));
	}

	static class Range {
		AbstractInsnNode fromInclusive;
		AbstractInsnNode toInclusive;
	}

	private final List<AbstractInsnNode> from = new ArrayList<AbstractInsnNode>();
	private final List<AbstractInsnNode> to = new ArrayList<AbstractInsnNode>();

	public void ignore(AbstractInsnNode from, AbstractInsnNode to) {
		this.from.add(from);
		this.to.add(to);
	}

}
