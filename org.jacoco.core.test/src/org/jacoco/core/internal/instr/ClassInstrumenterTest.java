/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertNull;

import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link ClassInstrumenter}.
 */
public class ClassInstrumenterTest {

	private IRuntime runtime;

	private ClassInstrumenter instrumenter;

	@Before
	public void setup() {
		runtime = new LoggerRuntime();
		instrumenter = new ClassInstrumenter(123, runtime, new ClassVisitor(
				Opcodes.ASM4) {
		});
	}

	@Test(expected = IllegalStateException.class)
	public void testInstrumentInstrumentedClass1() {
		instrumenter.visitField(InstrSupport.DATAFIELD_ACC,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC, null,
				null);
	}

	@Test(expected = IllegalStateException.class)
	public void testInstrumentInstrumentedClass2() {
		instrumenter.visitMethod(InstrSupport.INITMETHOD_ACC,
				InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC,
				null, null);
	}

	@Test
	public void testNoMethodVisitor() {
		instrumenter = new ClassInstrumenter(123, runtime, new ClassVisitor(
				Opcodes.ASM4) {
			@Override
			public MethodVisitor visitMethod(int access, String name,
					String desc, String signature, String[] exceptions) {
				return null;
			}
		});
		assertNull(instrumenter.visitMethod(0, "foo", "()V", null, null));
	}

}
