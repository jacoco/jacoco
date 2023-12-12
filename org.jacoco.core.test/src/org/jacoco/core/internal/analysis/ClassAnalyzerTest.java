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
package org.jacoco.core.internal.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link ClassAnalyzer}.
 */
public class ClassAnalyzerTest {

	private ClassAnalyzer analyzer;
	private ClassCoverageImpl coverage;

	@Before
	public void setup() {
		coverage = new ClassCoverageImpl("Foo", 0x0000, false);
		analyzer = new ClassAnalyzer(coverage, null, new StringPool());
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
		assertEquals(0, coverage.getMethods().size());
	}

	@Test
	public void should_collect_attributes() {
		assertTrue(analyzer.getClassAttributes().isEmpty());
		analyzer.visitAttribute(new Attribute("foo") {
		});
		assertTrue(analyzer.getClassAttributes().contains("foo"));
	}

}
