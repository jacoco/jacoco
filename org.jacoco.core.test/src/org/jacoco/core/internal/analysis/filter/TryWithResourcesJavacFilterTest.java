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
 * Unit tests for {@link TryWithResourcesJavacFilter}.
 */
public class TryWithResourcesJavacFilterTest extends FilterTestBase {

	private final TryWithResourcesJavacFilter filter = new TryWithResourcesJavacFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	/**
	 * javac 9 for
	 *
	 * <pre>
	 *     try (r0 = open(...); r1 = new ...) {
	 *         return ...
	 *     } finally {
	 *         ...
	 *     }
	 * </pre>
	 *
	 * generates
	 *
	 * <pre>
	 *     ...
	 *     ASTORE r0
	 *     ACONST_NULL
	 *     ASTORE primaryExc0
	 *
	 *     ...
	 *     ASTORE r1
	 *     ACONST_NULL
	 *     ASTORE primaryExc1
	 *
	 *     ... // body
	 *
	 *     ALOAD primaryExc1
	 *     ALOAD r1
	 *     INVOKESTATIC $closeResource:(Ljava/lang/Throwable;Ljava/lang/AutoCloseable;)V
	 *
	 *     ALOAD r0
	 *     IFNULL n
	 *     ALOAD primaryExc0
	 *     ALOAD r0
	 *     INVOKESTATIC $closeResource:(Ljava/lang/Throwable;Ljava/lang/AutoCloseable;)V
	 *     n:
	 *
	 *     ... // finally on normal path
	 *     ARETURN
	 *
	 *     ASTORE t
	 *     ALOAD t
	 *     ASTORE primaryExc1
	 *     ALOAD t
	 *     ATHROW
	 *
	 *     ASTORE t
	 *     ALOAD primaryExc1
	 *     ALOAD r1
	 *     INVOKESTATIC  $closeResource:(Ljava/lang/Throwable;Ljava/lang/AutoCloseable;)V
	 *     ALOAD t
	 *     ATHROW
	 *
	 *     ASTORE t
	 *     ALOAD t
	 *     ASTORE primaryExc0
	 *     ALOAD t
	 *     ATHROW
	 *
	 *     ASTORE t
	 *     ALOAD r0
	 *     IFNULL n
	 *     ALOAD primaryExc0
	 *     ALOAD r0
	 *     INVOKESTATIC  $closeResource:(Ljava/lang/Throwable;Ljava/lang/AutoCloseable;)V
	 *     n:
	 *     ALOAD t
	 *     ATHROW
	 *
	 *     ... // additional handlers for catch blocks and finally on exceptional path
	 * </pre>
	 */
	@Test
	public void javac9() {
		final Range range0 = new Range();
		final Range range1 = new Range();
		final Range range2 = new Range();
		final Range range3 = new Range();

		final Label handler1 = new Label();
		m.visitTryCatchBlock(handler1, handler1, handler1,
				"java/lang/Throwable");

		final Label handler2 = new Label();
		m.visitTryCatchBlock(handler2, handler2, handler2,
				"java/lang/Throwable");

		// r0 = open(...)
		m.visitVarInsn(Opcodes.ASTORE, 1);

		// primaryExc0 = null
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitVarInsn(Opcodes.ASTORE, 2);

		// r1 = new ..
		m.visitVarInsn(Opcodes.ASTORE, 3);

		// primaryExc1 = null
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitVarInsn(Opcodes.ASTORE, 4);

		// body
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitVarInsn(Opcodes.ASTORE, 5);

		// $closeResource(primaryExc1, r1)
		m.visitVarInsn(Opcodes.ALOAD, 4);
		range0.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Fun", "$closeResource",
				"(Ljava/lang/Throwable;Ljava/lang/AutoCloseable;)V", false);
		range0.toInclusive = m.instructions.getLast();

		// if (r0 != null)
		m.visitVarInsn(Opcodes.ALOAD, 1);
		range2.fromInclusive = m.instructions.getLast();
		final Label l11 = new Label();
		m.visitJumpInsn(Opcodes.IFNULL, l11);
		// $closeResource(primaryExc0, r0)
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Fun", "$closeResource",
				"(Ljava/lang/Throwable;Ljava/lang/AutoCloseable;)V", false);
		range2.toInclusive = m.instructions.getLast();
		m.visitLabel(l11);

		// finally
		m.visitInsn(Opcodes.NOP);
		m.visitVarInsn(Opcodes.ALOAD, 5);
		m.visitInsn(Opcodes.ARETURN);

		// catch (Throwable t)
		m.visitLabel(handler1);
		range1.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 5);
		// primaryExc1 = t
		m.visitVarInsn(Opcodes.ALOAD, 5);
		m.visitVarInsn(Opcodes.ASTORE, 4);
		// throw t
		m.visitVarInsn(Opcodes.ALOAD, 5);
		m.visitInsn(Opcodes.ATHROW);

		// catch (any t)
		m.visitVarInsn(Opcodes.ASTORE, 6);
		// $closeResource(primaryExc1, r1)
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Fun", "$closeResource",
				"(Ljava/lang/Throwable;Ljava/lang/AutoCloseable;)V", false);
		m.visitVarInsn(Opcodes.ALOAD, 6);
		m.visitInsn(Opcodes.ATHROW);
		range1.toInclusive = m.instructions.getLast();

		// catch (Throwable t)
		m.visitLabel(handler2);
		range3.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 3);
		// primaryExc0 = t
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		// throw t
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitInsn(Opcodes.ATHROW);

		// catch (any t)
		m.visitVarInsn(Opcodes.ASTORE, 7);
		// if (r0 != null)
		m.visitVarInsn(Opcodes.ALOAD, 1);
		final Label l14 = new Label();
		m.visitJumpInsn(Opcodes.IFNULL, l14);
		// $closeResource(primaryExc0, r0)
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Fun", "$closeResource",
				"(Ljava/lang/Throwable;Ljava/lang/AutoCloseable;)V", false);
		m.visitLabel(l14);
		// throw t
		m.visitVarInsn(Opcodes.ALOAD, 7);
		m.visitInsn(Opcodes.ATHROW);
		range3.toInclusive = m.instructions.getLast();

		m.visitVarInsn(Opcodes.ASTORE, 8);
		// finally
		m.visitInsn(Opcodes.NOP);
		m.visitVarInsn(Opcodes.ALOAD, 8);
		m.visitInsn(Opcodes.ATHROW);

		filter.filter(m, context, output);

		assertIgnored(range0, range1, range2, range3);
	}

	/**
	 * javac 7 and 8 for
	 *
	 * <pre>
	 *     try (r0 = ...; r1 = ...) {
	 *         return ...
	 *     } finally {
	 *         ...
	 *     }
	 * </pre>
	 *
	 * generate
	 *
	 * <pre>
	 *     ...
	 *     ASTORE r0
	 *     ACONST_NULL
	 *     ASTORE primaryExc0
	 *
	 *     ...
	 *     ASTORE r1
	 *     ACONST_NULL
	 *     ASTORE primaryExc1
	 *
	 *     ... // body
	 *
	 *     ALOAD r1
	 *     IFNULL n
	 *     ALOAD primaryExc1
	 *     IFNULL c
	 *     ALOAD r1
	 *     INVOKEINTERFACE close:()V
	 *     GOTO n
	 *     ASTORE t
	 *     ALOAD primaryExc1
	 *     ALOAD t
	 *     INVOKEVIRTUAL java/lang/Throwable.addSuppressed:(Ljava/lang/Throwable;)V
	 *     GOTO n
	 *     c:
	 *     ALOAD r1
	 *     INVOKEINTERFACE close:()V
	 *     n:
	 *
	 *     ALOAD r0
	 *     IFNULL n
	 *     ALOAD primaryExc0
	 *     IFNULL c
	 *     ALOAD r0
	 *     INVOKEVIRTUAL close:()V
	 *     GOTO n
	 *     ASTORE t
	 *     ALOAD primaryExc0
	 *     ALOAD t
	 *     INVOKEVIRTUAL java/lang/Throwable.addSuppressed:(Ljava/lang/Throwable;)V
	 *     GOTO n
	 *     c:
	 *     ALOAD r0
	 *     INVOKEVIRTUAL close:()V
	 *     n:
	 *
	 *     ... // finally on normal path
	 *     ARETURN
	 *
	 *     ASTORE t
	 *     ALOAD t
	 *     ASTORE primaryExc1
	 *     ALOAD t
	 *     ATHROW
	 *
	 *     ASTORE t1
	 *     ALOAD r1
	 *     IFNULL e
	 *     ALOAD primaryExc1
	 *     IFNULL c
	 *     ALOAD r1
	 *     INVOKEINTERFACE close:()V
	 *     GOTO e
	 *     ASTORE t2
	 *     ALOAD primaryExc1
	 *     ALOAD t2
	 *     INVOKEVIRTUAL java/lang/Throwable.addSuppressed:(Ljava/lang/Throwable;)V
	 *     GOTO e
	 *     c:
	 *     ALOAD r1
	 *     INVOKEINTERFACE close:()V
	 *     e:
	 *     ALOAD t1
	 *     ATHROW
	 *
	 *     ASTORE t
	 *     ALOAD t
	 *     ASTORE primaryExc0
	 *     ALOAD t
	 *     ATHROW
	 *
	 *     ASTORE t1
	 *     ALOAD r0
	 *     IFNULL e
	 *     ALOAD primaryExc0
	 *     IFNULL c
	 *     ALOAD r0
	 *     INVOKEVIRTUAL close:()V
	 *     GOTO e
	 *     ASTORE t2
	 *     ALOAD primaryExc0
	 *     ALOAD t2
	 *     INVOKEVIRTUAL java/lang/Throwable.addSuppressed:(Ljava/lang/Throwable;)V
	 *     GOTO e
	 *     c:
	 *     ALOAD r0
	 *     INVOKEVIRTUAL close:()V
	 *     e:
	 *     ALOAD t1
	 *     ATHROW
	 *
	 *     ... // additional handlers for catch blocks and finally on exceptional path
	 * </pre>
	 */
	@Test
	public void javac_7_8() {
		final Range range0 = new Range();
		final Range range1 = new Range();
		final Range range2 = new Range();
		final Range range3 = new Range();

		final Label handler1 = new Label();
		m.visitTryCatchBlock(handler1, handler1, handler1,
				"java/lang/Throwable");
		final Label handler2 = new Label();
		m.visitTryCatchBlock(handler2, handler2, handler2,
				"java/lang/Throwable");

		// r1 = ...
		m.visitVarInsn(Opcodes.ASTORE, 1);

		// primaryExc1 = null
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitVarInsn(Opcodes.ASTORE, 2);

		// r2 = ...
		m.visitVarInsn(Opcodes.ASTORE, 3);
		// primaryExc2 = null
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitVarInsn(Opcodes.ASTORE, 4);

		// body
		m.visitInsn(Opcodes.NOP);

		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitVarInsn(Opcodes.ASTORE, 5);

		final Label l15 = new Label();
		// if (r2 != null)
		m.visitVarInsn(Opcodes.ALOAD, 3);
		range0.fromInclusive = m.instructions.getLast();
		m.visitJumpInsn(Opcodes.IFNULL, l15);
		// if (primaryExc2 != null)
		m.visitVarInsn(Opcodes.ALOAD, 4);
		final Label l26 = new Label();
		m.visitJumpInsn(Opcodes.IFNULL, l26);
		// r2.close
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "Fun$Resource2", "close",
				"()V", false);
		m.visitJumpInsn(Opcodes.GOTO, l15);

		m.visitVarInsn(Opcodes.ASTORE, 6);
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitVarInsn(Opcodes.ALOAD, 6);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
				"addSuppressed", "(Ljava/lang/Throwable;)V", false);
		m.visitJumpInsn(Opcodes.GOTO, l15);

		m.visitLabel(l26);

		// r2.close
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "Fun$Resource2", "close",
				"()V", false);
		range0.toInclusive = m.instructions.getLast();
		m.visitLabel(l15);

		// if (r1 != null)
		m.visitVarInsn(Opcodes.ALOAD, 1);
		range2.fromInclusive = m.instructions.getLast();
		final Label l23 = new Label();
		m.visitJumpInsn(Opcodes.IFNULL, l23);
		// if (primaryExc1 != null)
		m.visitVarInsn(Opcodes.ALOAD, 2);
		final Label l27 = new Label();
		m.visitJumpInsn(Opcodes.IFNULL, l27);
		// r1.close
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun$Resource1", "close",
				"()V", false);
		m.visitJumpInsn(Opcodes.GOTO, l23);

		m.visitVarInsn(Opcodes.ASTORE, 6);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ALOAD, 6);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
				"addSuppressed", "(Ljava/lang/Throwable;)V", false);
		m.visitJumpInsn(Opcodes.GOTO, l23);

		m.visitLabel(l27);
		// r1.close
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun$Resource1", "close",
				"()V", false);
		range2.toInclusive = m.instructions.getLast();
		m.visitLabel(l23);

		// finally
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.ARETURN);

		// catch (Throwable t)
		m.visitLabel(handler1);
		range1.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 5);
		// primaryExc2 = t
		m.visitVarInsn(Opcodes.ALOAD, 5);
		m.visitVarInsn(Opcodes.ASTORE, 4);
		// throw t
		m.visitVarInsn(Opcodes.ALOAD, 5);
		m.visitInsn(Opcodes.ATHROW);

		// catch (any t)
		m.visitVarInsn(Opcodes.ASTORE, 7);
		// if (r2 != null)
		m.visitVarInsn(Opcodes.ALOAD, 3);
		final Label l28 = new Label();
		m.visitJumpInsn(Opcodes.IFNULL, l28);
		// if (primaryExc2 != null)
		m.visitVarInsn(Opcodes.ALOAD, 4);
		final Label l29 = new Label();
		m.visitJumpInsn(Opcodes.IFNULL, l29);
		// r2.close
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "Fun$Resource2", "close",
				"()V", false);
		m.visitJumpInsn(Opcodes.GOTO, l28);

		m.visitVarInsn(Opcodes.ASTORE, 8);
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitVarInsn(Opcodes.ALOAD, 8);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
				"addSuppressed", "(Ljava/lang/Throwable;)V", false);
		m.visitJumpInsn(Opcodes.GOTO, l28);

		m.visitLabel(l29);
		// r2.close
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "Fun$Resource2", "close",
				"()V", false);
		m.visitLabel(l28);
		// throw t
		m.visitVarInsn(Opcodes.ALOAD, 7);
		m.visitInsn(Opcodes.ATHROW);
		range1.toInclusive = m.instructions.getLast();

		// catch (Throwable t)
		m.visitLabel(handler2);
		range3.fromInclusive = m.instructions.getLast();
		m.visitVarInsn(Opcodes.ASTORE, 3);
		// primaryExc2 = t
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		// throw t
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitInsn(Opcodes.ATHROW);

		// catch (any t)
		m.visitVarInsn(Opcodes.ASTORE, 9);
		// if (r1 != null)
		m.visitVarInsn(Opcodes.ALOAD, 1);
		final Label l30 = new Label();
		m.visitJumpInsn(Opcodes.IFNULL, l30);
		// if (primaryExc1 != null)
		m.visitVarInsn(Opcodes.ALOAD, 2);
		final Label l31 = new Label();
		m.visitJumpInsn(Opcodes.IFNULL, l31);
		// r1.close
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun$Resource1", "close",
				"()V", false);
		m.visitJumpInsn(Opcodes.GOTO, l30);

		m.visitVarInsn(Opcodes.ASTORE, 10);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ALOAD, 10);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
				"addSuppressed", "(Ljava/lang/Throwable;)V", false);
		m.visitJumpInsn(Opcodes.GOTO, l30);

		m.visitLabel(l31);
		// r1.close
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun$Resource1", "close",
				"()V", false);
		m.visitLabel(l30);
		// throw t
		m.visitVarInsn(Opcodes.ALOAD, 9);
		m.visitInsn(Opcodes.ATHROW);
		range3.toInclusive = m.instructions.getLast();

		m.visitVarInsn(Opcodes.ASTORE, 11);
		// finally
		m.visitInsn(Opcodes.NOP);
		m.visitVarInsn(Opcodes.ALOAD, 11);
		m.visitInsn(Opcodes.ATHROW);

		filter.filter(m, context, output);

		assertIgnored(range0, range1, range2, range3);
	}

	/**
	 * javac 9 for
	 *
	 * <pre>
	 *     try (r = new ...) {
	 *         ...
	 *     } finally {
	 *         ...
	 *     }
	 * </pre>
	 *
	 * generates
	 *
	 * <pre>
	 *     ...
	 *     ASTORE r
	 *     ACONST_NULL
	 *     ASTORE primaryExc
	 *
	 *     ... // body
	 *
	 *     ALOAD primaryExc
	 *     IFNULL c
	 *     ALOAD r
	 *     INVOKEVIRTUAL close:()V
	 *     GOTO f
	 *     ASTORE t
	 *     ALOAD primaryExc
	 *     ALOAD t
	 *     NVOKEVIRTUAL java/lang/Throwable.addSuppressed:(Ljava/lang/Throwable;)V
	 *     GOTO f
	 *     c:
	 *     ALOAD r
	 *     INVOKEVIRTUAL close:()V
	 *     GOTO f
	 *
	 *     ASTORE t
	 *     ALOAD t
	 *     ASTORE primaryExc
	 *     ALOAD t
	 *     ATHROW
	 *
	 *     ASTORE t
	 *     ALOAD primaryExc
	 *     IFNULL c
	 *     ALOAD r
	 *     INVOKEVIRTUAL close:()V
	 *     GOTO L78
	 *     ASTORE t2
	 *     ALOAD primaryExc
	 *     ALOAD t2
	 *     INVOKEVIRTUAL java/lang/Throwable.addSuppressed:(Ljava/lang/Throwable;)V
	 *     goto e
	 *     c:
	 *     ALOAD r
	 *     INVOKEVIRTUAL close:()V
	 *     e:
	 *     ALOAD t
	 *     ATHROW
	 *
	 *     f:
	 *     ... // finally on normal path
	 *     ... // additional handlers for catch blocks and finally on exceptional path
	 *     ...
	 * </pre>
	 */
	@Test
	public void javac9_omitted_null_check() {
		final Range range0 = new Range();
		final Range range1 = new Range();

		final Label handler = new Label();
		m.visitTryCatchBlock(handler, handler, handler, "java/lang/Throwable");

		// primaryExc = null
		m.visitInsn(Opcodes.ACONST_NULL);
		m.visitVarInsn(Opcodes.ASTORE, 1);

		// r = new ...
		m.visitInsn(Opcodes.NOP);

		final Label end = new Label();
		// "finally" on a normal path
		{
			// if (primaryExc != null)
			m.visitVarInsn(Opcodes.ALOAD, 2);
			range0.fromInclusive = m.instructions.getLast();
			final Label closeLabel = new Label();
			m.visitJumpInsn(Opcodes.IFNULL, closeLabel);
			// r.close
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Resource", "close", "()V",
					false);
			m.visitJumpInsn(Opcodes.GOTO, end);

			// catch (Throwable t)
			m.visitVarInsn(Opcodes.ASTORE, 3);
			// primaryExc.addSuppressed(t)
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitVarInsn(Opcodes.ALOAD, 3);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
					"addSuppressed", "(Ljava/lang/Throwable;)V", false);
			m.visitJumpInsn(Opcodes.GOTO, end);

			m.visitLabel(closeLabel);
			// r.close()
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Resource", "close", "()V",
					false);
		}
		m.visitJumpInsn(Opcodes.GOTO, end);
		range0.toInclusive = m.instructions.getLast();
		// catch (Throwable t)
		m.visitLabel(handler);
		{
			range1.fromInclusive = m.instructions.getLast();
			m.visitVarInsn(Opcodes.ASTORE, 3);
			// primaryExc = t
			m.visitVarInsn(Opcodes.ALOAD, 3);
			m.visitVarInsn(Opcodes.ASTORE, 2);
			// throw t
			m.visitVarInsn(Opcodes.ALOAD, 3);
			m.visitInsn(Opcodes.ATHROW);
		}
		// catch (any t)
		m.visitVarInsn(Opcodes.ASTORE, 4);
		// "finally" on exceptional path
		{
			// if (primaryExc != null)
			m.visitVarInsn(Opcodes.ALOAD, 2);
			final Label closeLabel = new Label();
			m.visitJumpInsn(Opcodes.IFNULL, closeLabel);
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Resource", "close", "()V",
					false);
			final Label finallyEndLabel = new Label();
			m.visitJumpInsn(Opcodes.GOTO, finallyEndLabel);

			// catch (Throwable t)
			m.visitVarInsn(Opcodes.ASTORE, 5);
			// primaryExc.addSuppressed(t)
			m.visitVarInsn(Opcodes.ALOAD, 2);
			m.visitVarInsn(Opcodes.ALOAD, 5);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Throwable",
					"addSuppressed", "(Ljava/lang/Throwable;)V", false);
			m.visitJumpInsn(Opcodes.GOTO, finallyEndLabel);

			m.visitLabel(closeLabel);
			// r.close()
			m.visitVarInsn(Opcodes.ALOAD, 1);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Resource", "close", "()V",
					false);
			m.visitLabel(finallyEndLabel);
		}
		// throw t
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitInsn(Opcodes.ATHROW);
		range1.toInclusive = m.instructions.getLast();

		m.visitLabel(end);

		filter.filter(m, context, output);

		assertIgnored(range0, range1);
	}

	/**
	 * javac 9 for
	 *
	 * <pre>
	 *     try (r = new ...) {
	 *       throw ...
	 *     }
	 * </pre>
	 *
	 * generates
	 *
	 * <pre>
	 *     ...
	 *     ASTORE r
	 *     ACONST_NULL
	 *     ASTORE primaryExc
	 *
	 *     ...
	 *     ATHROW
	 *
	 *     ASTORE t
	 *     ALOAD t
	 *     ASTORE primaryExc
	 *     ALOAD t
	 *     ATHROW
	 *
	 *     ASTORE t
	 *     ALOAD primaryExc
	 *     ALOAD r
	 *     INVOKESTATIC  $closeResource:(Ljava/lang/Throwable;Ljava/lang/AutoCloseable;)V
	 *     ALOAD t
	 *     ATHROW
	 * </pre>
	 */
	@Test
	public void only_exceptional_path() {
		final Label start = new Label();
		final Label handler = new Label();
		m.visitTryCatchBlock(start, handler, handler, "java/lang/Throwable");

		m.visitLabel(start);
		m.visitInsn(Opcodes.ATHROW);
		m.visitLabel(handler);
		m.visitVarInsn(Opcodes.ASTORE, 3);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitVarInsn(Opcodes.ASTORE, 2);
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitInsn(Opcodes.ATHROW);

		m.visitVarInsn(Opcodes.ASTORE, 4);
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Fun", "$closeResource",
				"(Ljava/lang/Throwable;Ljava/lang/AutoCloseable;)V", false);
		m.visitVarInsn(Opcodes.ALOAD, 4);
		m.visitInsn(Opcodes.ATHROW);

		filter.filter(m, context, output);

		assertIgnored();
	}

}
