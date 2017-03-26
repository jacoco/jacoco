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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.internal.Java9Support;
import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.TraceMethodVisitor;

public class TryWithResourcesFilterTest implements IFilterOutput {

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"name", "()V", null, null);

	private static void asmify(String filename) throws IOException {
		final byte[] bytes = Java9Support.downgrade(
				Java9Support.readFully(new FileInputStream(filename)));
		final ClassReader cr = new ClassReader(bytes);
		cr.accept(
				new TraceClassVisitor(null, new ASMifier(),
						new PrintWriter(System.out)),
				ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
	}

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
	 */
	@Test
	public void test1() {
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
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Fun", "$closeResource",
				"(Ljava/lang/Throwable;Ljava/lang/AutoCloseable;)V", false);

		// if (r0 != null)
		m.visitVarInsn(Opcodes.ALOAD, 1);
		final Label l11 = new Label();
		m.visitJumpInsn(Opcodes.IFNULL, l11);
		// $closeResource(primaryExc0, r0)
		m.visitVarInsn(Opcodes.ALOAD, 2);
		m.visitVarInsn(Opcodes.ALOAD, 1);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Fun", "$closeResource",
				"(Ljava/lang/Throwable;Ljava/lang/AutoCloseable;)V", false);
		m.visitLabel(l11);

		// finally
		m.visitInsn(Opcodes.NOP);
		m.visitVarInsn(Opcodes.ALOAD, 5);
		m.visitInsn(Opcodes.ARETURN);

		// catch (Throwable t)
		m.visitLabel(handler1);
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

		// catch (Throwable t)
		m.visitLabel(handler2);
		m.visitVarInsn(Opcodes.ASTORE, 3);
		// primaryExc1 = t
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

		m.visitVarInsn(Opcodes.ASTORE, 8);
		// finally
		m.visitInsn(Opcodes.NOP);
		m.visitVarInsn(Opcodes.ALOAD, 8);
		m.visitInsn(Opcodes.ATHROW);

		print();
		new TryWithResourcesFilter().filter(m, this);

		assertEquals(4, from.size());

		assertEquals(m.instructions.get(9), from.get(0));
		assertEquals(m.instructions.get(11), to.get(0));

		assertEquals(m.instructions.get(22), from.get(1));
		assertEquals(m.instructions.get(32), to.get(1));

		assertEquals(m.instructions.get(12), from.get(2));
		assertEquals(m.instructions.get(16), to.get(2));

		assertEquals(m.instructions.get(34), from.get(3));
		assertEquals(m.instructions.get(47), to.get(3));
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
	 */
	@Test
	public void test2() {
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
		m.visitJumpInsn(Opcodes.IFNULL, l15);
		// if (primaryExc2 != null)
		m.visitVarInsn(Opcodes.ALOAD, 4);
		final Label l26 = new Label();
		m.visitJumpInsn(Opcodes.IFNULL, l26);
		// r2.close
		m.visitVarInsn(Opcodes.ALOAD, 3);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun$Resource2", "close",
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
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun$Resource2", "close",
				"()V", false);
		m.visitLabel(l15);

		// if (r1 != null)
		m.visitVarInsn(Opcodes.ALOAD, 1);
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
		m.visitLabel(l23);

		// finally
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.ARETURN);

		// catch (Throwable t)
		m.visitLabel(handler1);
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
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun$Resource2", "close",
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
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun$Resource2", "close",
				"()V", false);
		m.visitLabel(l28);
		// throw t
		m.visitVarInsn(Opcodes.ALOAD, 7);
		m.visitInsn(Opcodes.ATHROW);

		// catch (Throwable t)
		m.visitLabel(handler2);
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

		m.visitVarInsn(Opcodes.ASTORE, 11);
		// finally
		m.visitInsn(Opcodes.NOP);
		m.visitVarInsn(Opcodes.ALOAD, 11);
		m.visitInsn(Opcodes.ATHROW);

		print();
		new TryWithResourcesFilter().filter(m, this);

		assertEquals(4, from.size());

		assertEquals(m.instructions.get(9), from.get(0));
		assertEquals(m.instructions.get(23), to.get(0));

		assertEquals(m.instructions.get(44), from.get(1));
		assertEquals(m.instructions.get(67), to.get(1));

		assertEquals(m.instructions.get(25), from.get(2));
		assertEquals(m.instructions.get(39), to.get(2));

		assertEquals(m.instructions.get(69), from.get(3));
		assertEquals(m.instructions.get(92), to.get(3));
	}

	@Test
	public void javac9_omitted_null_check() {
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
		// catch (Throwable t)
		m.visitLabel(handler);
		{
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

		m.visitLabel(end);

		print();
		new TryWithResourcesFilter().filter(m, this);

		assertEquals(2, from.size());

		assertEquals(m.instructions.get(3), from.get(0));
		assertEquals(m.instructions.get(16), to.get(0));

		assertEquals(m.instructions.get(18), from.get(1));
		assertEquals(m.instructions.get(39), to.get(1));
	}

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
			m.visitJumpInsn(Opcodes.IFNULL, l4);
			m.visitVarInsn(Opcodes.ALOAD, 5);
			m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Fun2$Resource", "close",
					"()V", false);
		}
		m.visitJumpInsn(Opcodes.GOTO, l4);
		// catch (any primaryExc)
		m.visitLabel(handler);
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

		// additional handlers
		m.visitInsn(Opcodes.NOP);

		print();
		new TryWithResourcesFilter().filter(m, this);

		assertEquals(1, from.size());

		assertEquals(m.instructions.get(5), from.get(0));
		assertEquals(m.instructions.get(88), to.get(0));
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
			m.visitLabel(label);
		}

		// finally
		m.visitInsn(Opcodes.NOP);
		m.visitInsn(Opcodes.ARETURN);

		// catch (any primaryExc)
		m.visitLabel(handler);
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

		// additional handlers
		m.visitInsn(Opcodes.NOP);

		print();
		new TryWithResourcesFilter().filter(m, this);

		assertEquals(2, from.size());

		assertEquals(m.instructions.get(5), from.get(0));
		assertEquals(m.instructions.get(18), to.get(0));

		assertEquals(m.instructions.get(22), from.get(1));
		assertEquals(m.instructions.get(88), to.get(1));
	}

	private void print() {
		final PrintWriter pw = new PrintWriter(System.out);
		final TraceMethodVisitor mv = new TraceMethodVisitor(new Textifier());
		for (int i = 0; i < m.instructions.size(); i++) {
			m.instructions.get(i).accept(mv);
			pw.format("%3d: @%-8s", i,
					Integer.toHexString(m.instructions.get(i).hashCode()));
			mv.p.print(pw);
			mv.p.getText().clear();
			pw.flush();
		}
	}

	private final List<AbstractInsnNode> from = new ArrayList<AbstractInsnNode>();
	private final List<AbstractInsnNode> to = new ArrayList<AbstractInsnNode>();

	public void ignore(AbstractInsnNode from, AbstractInsnNode to) {
		this.from.add(from);
		this.to.add(to);
	}
}
