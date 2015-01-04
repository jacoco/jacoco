/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link ClassAnalyzer}.
 */
public class ClassAnalyzerTest {

	private ClassAnalyzer analyzer;

	@Before
	public void setup() {
		analyzer = new ClassAnalyzer(0x0000, false, null, new StringPool());
		analyzer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Foo", null,
				"java/lang/Object", null);
	}

	@Test(expected = IllegalStateException.class)
	public void testAnalyzeInstrumentedClass1() {
		analyzer.visitField(InstrSupport.DATAFIELD_ACC,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC, null,
				null);
	}

	@Test(expected = IllegalStateException.class)
	public void testAnalyzeInstrumentedClass2() {
		analyzer.visitMethod(InstrSupport.INITMETHOD_ACC,
				InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC,
				null, null);
	}

	@Test
	public void testMethodFilter_Empty() {
		final MethodProbesVisitor mv = analyzer.visitMethod(0, "foo", "()V",
				null, null);
		mv.visitEnd();
		Collection<IMethodCoverage> methods = analyzer.getCoverage()
				.getMethods();
		assertEquals(0, methods.size());
	}

	@Test
	public void testMethodFilter_NonSynthetic() {
		final MethodProbesVisitor mv = analyzer.visitMethod(0, "foo", "()V",
				null, null);
		mv.visitCode();
		mv.visitInsn(Opcodes.RETURN);
		mv.visitEnd();
		Collection<IMethodCoverage> methods = analyzer.getCoverage()
				.getMethods();
		assertEquals(1, methods.size());
	}

	@Test
	public void testMethodFilter_Synthetic() {
		final MethodProbesVisitor mv = analyzer.visitMethod(
				Opcodes.ACC_SYNTHETIC, "foo", "()V", null, null);
		assertNull(mv);
		Collection<IMethodCoverage> methods = analyzer.getCoverage()
				.getMethods();
		assertTrue(methods.isEmpty());
	}

	@Test
	public void testMethodFilter_Lambda() {
		final MethodProbesVisitor mv = analyzer.visitMethod(
				Opcodes.ACC_SYNTHETIC, "lambda$1", "()V", null, null);
		mv.visitCode();
		mv.visitInsn(Opcodes.RETURN);
		mv.visitEnd();
		Collection<IMethodCoverage> methods = analyzer.getCoverage()
				.getMethods();
		assertEquals(1, methods.size());
	}

}
