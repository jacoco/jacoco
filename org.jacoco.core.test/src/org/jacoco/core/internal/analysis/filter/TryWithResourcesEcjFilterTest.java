/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link TryWithResourcesEcjFilter}.
 */
public class TryWithResourcesEcjFilterTest extends FilterTestBase {

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
	 *     ...
	 * </pre>
	 *
	 * generates
	 *
	 * <pre>
	 *     ACONST_NULL
	 *     ASTORE primaryExc
	 *     ACONST_NULL
	 *     ASTORE suppressedExc
	 *     ...
	 *     ASTORE r1
	 *     ...
	 *     ASTORE r2
	 *     ...
	 *     ASTORE r3
	 *
	 *     ... // body
	 *
	 *     ALOAD r3
	 *     IFNULL r2_close
	 *     ALOAD r3
	 *     INVOKEVIRTUAL close:()V
	 *     GOTO r2_close
	 *
	 *     ASTORE primaryExc
	 *     ALOAD r3
	 *     IFNULL n
	 *     ALOAD r3
	 *     INVOKEVIRTUAL close:()V
	 *     n:
	 *     ALOAD primaryExc
	 *     ATHROW
	 *
	 *     r2_close:
	 *     ALOAD r2
	 *     IFNULL r1_close
	 *     ALOAD r2
	 *     INVOKEVIRTUAL close:()V
	 *     GOTO r1_close
	 *
	 *     ASTORE suppressedExc
	 *     ALOAD primaryExc
	 *     IFNONNULL s
	 *     ALOAD suppressedExc
	 *     ASTORE primaryExc
	 *     GOTO e
	 *     s:
	 *     ALOAD primaryExc
	 *     ALOAD suppressedExc
	 *     IF_ACMPEQ e
	 *     ALOAD primaryExc
	 *     ALOAD suppressedExc
	 *     INVOKEVIRTUAL java/lang/Throwable.addSuppressed:(Ljava/lang/Throwable;)V
	 *     e:
	 *
	 *     ALOAD r2
	 *     IFNULL n
	 *     ALOAD r2
	 *     INVOKEVIRTUAL close:()V
	 *     n:
	 *     ALOAD primaryExc
	 *     ATHROW
	 *
	 *     r1_close:
	 *     ALOAD r1
	 *     IFNULL after
	 *     ALOAD r1
	 *     INVOKEVIRTUAL close:()V
	 *     GOTO after
	 *
	 *     ASTORE suppressedExc
	 *     ALOAD primaryExc
	 *     IFNONNULL s
	 *     ALOAD suppressedExc
	 *     ASTORE primaryExc
	 *     GOTO e
	 *     s:
	 *     ALOAD primaryExc
	 *     ALOAD suppressedExc
	 *     IF_ACMPEQ e
	 *     ALOAD primaryExc
	 *     ALOAD suppressedExc
	 *     INVOKEVIRTUAL java/lang/Throwable.addSuppressed:(Ljava/lang/Throwable;)V
	 *     e:
	 *
	 *     ALOAD r1
	 *     IFNULL n
	 *     ALOAD r1
	 *     INVOKEVIRTUAL close:()V
	 *     n:
	 *     ALOAD primaryExc
	 *     ATHROW
	 *
	 *     ASTORE suppressedExc
	 *     ALOAD primaryExc
	 *     IFNONNULL s
	 *     ALOAD suppressedExc
	 *     ASTORE primaryExc
	 *     GOTO e
	 *     s:
	 *     ALOAD primaryExc
	 *     ALOAD suppressedExc
	 *     IF_ACMPEQ e
	 *     ALOAD primaryExc
	 *     ALOAD suppressedExc
	 *     INVOKEVIRTUAL java/lang/Throwable.addSuppressed:(Ljava/lang/Throwable;)V
	 *     e:
	 *
	 *     ALOAD primaryExc
	 *     ATHROW
	 *
	 *     ... // additional handlers for catch blocks and finally on exceptional path
	 *
	 *     after:
	 *     ... // finally on normal path
	 *     ...
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

		filter.filter(m, context, output);

		assertIgnored(range0, range1);
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
	 *
	 * generates
	 *
	 * <pre>
	 *     ACONST_NULL
	 *     astore primaryExc
	 *     ACONST_NULL
	 *     astore suppressedExc
	 *
	 *     ...
	 *     ASTORE r1
	 *     ...
	 *     ASTORE r2
	 *     ...
	 *     ASTORE r3
	 *
	 *     ... // body
	 *
	 *     ALOAD r3
	 *     IFNULL n
	 *     ALOAD r3
	 *     INVOKEVIRTUAL close:()V
	 *     n:
	 *     ALOAD r2
	 *     IFNULL n
	 *     ALOAD r2
	 *     INVOKEVIRTUAL close:()V
	 *     n:
	 *     ALOAD r1
	 *     IFNULL n
	 *     ALOAD r1
	 *     INVOKEVIRTUAL close:()V
	 *     n:
	 *
	 *     ... // finally on normal path
	 *     ARETURN
	 *
	 *     ASTORE primaryExc
	 *     ALOAD r3
	 *     IFNULL n
	 *     ALOAD r3
	 *     INVOKEVIRTUAL close:()V
	 *     n:
	 *     ALOAD primaryExc
	 *     ATHROW
	 *
	 *     ASTORE suppressedExc
	 *     ALOAD primaryExc
	 *     IFNONNULL s
	 *     ALOAD suppressedExc
	 *     ASTORE primaryExc
	 *     GOTO  e
	 *     s:
	 *     ALOAD primaryExc
	 *     ALOAD suppressedExc
	 *     IF_ACMPEQ  e
	 *     ALOAD primaryExc
	 *     ALOAD suppressedExc
	 *     INVOKEVIRTUAL java/lang/Throwable.addSuppressed:(Ljava/lang/Throwable;)V
	 *     e:
	 *
	 *     ALOAD r2
	 *     IFNULL n
	 *     ALOAD r2
	 *     INVOKEVIRTUAL close:()V
	 *     n:
	 *     ALOAD primaryExc
	 *     ATHROW
	 *
	 *     ASTORE suppressedExc
	 *     ALOAD primaryExc
	 *     IFNONNULL s
	 *     ALOAD suppressedExc
	 *     ASTORE primaryExc
	 *     GOTO e
	 *     s:
	 *     ALOAD primaryExc
	 *     ALOAD suppressedExc
	 *     IF_ACMPEQ e
	 *     ALOAD primaryExc
	 *     ALOAD suppressedExc
	 *     INVOKEVIRTUAL java/lang/Throwable.addSuppressed:(Ljava/lang/Throwable;)V
	 *     e:
	 *
	 *     ALOAD r1
	 *     IFNULL n
	 *     ALOAD r1
	 *     INVOKEVIRTUAL close:()V
	 *     n:
	 *     ALOAD primaryExc
	 *     ATHROW
	 *
	 *     ASTORE suppressedExc
	 *     ALOAD primaryExc
	 *     IFNONNULL s
	 *     ALOAD suppressedExc
	 *     ASTORE primaryExc
	 *     GOTO e
	 *     s:
	 *     ALOAD primaryExc
	 *     ALOAD suppressedExc
	 *     IF_ACMPEQ e
	 *     ALOAD primaryExc
	 *     ALOAD suppressedExc
	 *     INVOKEVIRTUAL java/lang/Throwable.addSuppressed:(Ljava/lang/Throwable;)V
	 *     e:
	 *
	 *     ALOAD primaryExc
	 *     ATHROW
	 *
	 *     ... // additional handlers for catch blocks and finally on exceptional path
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

		filter.filter(m, context, output);

		assertIgnored(range0, range1);
	}

}
