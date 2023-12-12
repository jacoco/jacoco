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
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Unit tests for {@link ClassInstrumenter}.
 */
public class ClassInstrumenterTest implements IProbeArrayStrategy {

	private ClassInstrumenter instrumenter;

	@Before
	public void setup() {
		instrumenter = new ClassInstrumenter(this,
				new ClassVisitor(InstrSupport.ASM_API_VERSION) {
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
		instrumenter = new ClassInstrumenter(this,
				new ClassVisitor(InstrSupport.ASM_API_VERSION) {
					@Override
					public MethodVisitor visitMethod(int access, String name,
							String desc, String signature,
							String[] exceptions) {
						return null;
					}
				});
		assertNull(instrumenter.visitMethod(0, "foo", "()V", null, null));
	}

	// === IProbeArrayStrategy ===

	public int storeInstance(MethodVisitor mv, boolean clinit, int variable) {
		return 0;
	}

	public void addMembers(ClassVisitor cv, int probeCount) {
	}

}
