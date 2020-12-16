/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Gergely Fábián - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link SyntheticFilter}.
 */
public class ScalaLambdaGeneratedMethodsFilterTest extends FilterTestBase {

	private final ScalaLambdaGeneratedMethodsFilter filter = new ScalaLambdaGeneratedMethodsFilter();

	@Test
	public void testNonScala() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void testScalaNop() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"name", "()V", null, null);
		context.classAttributes.add("Scala");

		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void testScalaIsInit() {
		// Only Scala 2.11.
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION,
				Opcodes.ACC_PUBLIC, "<init>",
				"(Lmypackage/Foo$$anonfun$testLambdas$1$$anonfun$apply$2$$anonfun$apply$3;I)V",
				null, null);
		context.classAttributes.add("Scala");
		context.className = "mypackage/Foo$$anonfun$testLambdas$1$$anonfun$apply$2$$anonfun$apply$3$$anonfun$apply$1";
		context.superClassName = "scala/runtime/AbstractFunction1$mcII$sp";

		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

}
