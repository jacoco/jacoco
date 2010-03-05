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
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.instr;

import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

/**
 * Unit tests for {@link ClassInstrumenter}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ClassInstrumenterTest {

	private IRuntime runtime;

	private ClassInstrumenter instrumenter;

	@Before
	public void setup() {
		runtime = new LoggerRuntime();
		instrumenter = new ClassInstrumenter(123, runtime, new EmptyVisitor());
	}

	@Test(expected = IllegalStateException.class)
	public void testInstrumentInstrumentedClass() {
		generateClass(new ClassInstrumenter(123, runtime, instrumenter));
	}

	private void generateClass(ClassVisitor visitor) {

		final String className = "org/jacoco/test/targets/ClassInstrumenterTestTarget";
		visitor.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, className, null,
				"java/lang/Object", new String[] {});

		// Constructor
		GeneratorAdapter gen = new GeneratorAdapter(visitor.visitMethod(
				Opcodes.ACC_PUBLIC, "<init>", "()V", null, new String[0]),
				Opcodes.ACC_PUBLIC, "<init>", "()V");
		gen.visitCode();
		gen.loadThis();
		gen.invokeConstructor(Type.getType(Object.class), new Method("<init>",
				"()V"));
		gen.returnValue();
		gen.visitMaxs(0, 0);
		gen.visitEnd();

		visitor.visitEnd();
	}

}
