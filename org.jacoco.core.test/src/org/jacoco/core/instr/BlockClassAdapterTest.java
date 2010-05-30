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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * Unit tests for {@link BlockClassAdapter}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class BlockClassAdapterTest {

	private static class MockVisitor extends EmptyVisitor implements
			IBlockClassVisitor {

		int count;

		@Override
		public IBlockMethodVisitor visitMethod(int access, String name,
				String desc, String signature, String[] exceptions) {
			return null;
		}

		public void visitTotalProbeCount(int count) {
			this.count = count;
		}
	}

	@Test
	public void testProbeCounter() {
		final MockVisitor mv = new MockVisitor();
		final BlockClassAdapter adapter = new BlockClassAdapter(mv);
		assertEquals(0, adapter.nextId());
		assertEquals(1, adapter.nextId());
		assertEquals(2, adapter.nextId());
		adapter.visitEnd();
		assertEquals(3, mv.count);
	}

	@Test
	public void testVisitMethod() {
		final MockVisitor mv = new MockVisitor() {
			@Override
			public IBlockMethodVisitor visitMethod(int access, String name,
					String desc, String signature, String[] exceptions) {
				class MockMethodVisitor extends EmptyVisitor implements
						IBlockMethodVisitor {
					public void visitBlockEnd(int id) {
					}

					public void visitBlockEndBeforeJump(int id) {
					}
				}
				return new MockMethodVisitor();
			}
		};
		final BlockClassAdapter adapter = new BlockClassAdapter(mv);
		writeMethod(adapter);
		writeMethod(adapter);
		writeMethod(adapter);
		adapter.visitEnd();
		assertEquals(3, mv.count);
	}

	@Test
	public void testVisitMethodNullMethodVisitor() {
		final MockVisitor mv = new MockVisitor();
		final BlockClassAdapter adapter = new BlockClassAdapter(mv);
		writeMethod(adapter);
		writeMethod(adapter);
		writeMethod(adapter);
		adapter.visitEnd();
		assertEquals(3, mv.count);
	}

	private void writeMethod(final ClassVisitor cv) {
		MethodVisitor mv = cv.visitMethod(0, "foo", "V()", null, null);
		mv.visitCode();
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 1);
		mv.visitEnd();
	}
}
