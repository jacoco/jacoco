/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Unit tests for {@link BlockClassAdapter}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class BlockClassAdapterTest {

	private MockAdapter adapter;

	private static class MockAdapter extends BlockClassAdapter {

		private final List<String> nonAbstractMethods = new ArrayList<String>();

		private final List<String> abstractMethods = new ArrayList<String>();

		void assertNonAbstractMethods(String... expected) {
			assertEquals(Arrays.asList(expected), nonAbstractMethods);
		}

		void assertAbstractMethods(String... expected) {
			assertEquals(Arrays.asList(expected), abstractMethods);
		}

		@Override
		protected IBlockMethodVisitor visitNonAbstractMethod(int access,
				String name, String desc, String signature, String[] exceptions) {
			nonAbstractMethods.add(name);
			return null;
		}

		@Override
		protected MethodVisitor visitAbstractMethod(int access, String name,
				String desc, String signature, String[] exceptions) {
			abstractMethods.add(name);
			return null;
		}

		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
		}

		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return null;
		}

		public void visitAttribute(Attribute attr) {
		}

		public FieldVisitor visitField(int access, String name, String desc,
				String signature, Object value) {
			return null;
		}

		public void visitInnerClass(String name, String outerName,
				String innerName, int access) {
		}

		public void visitOuterClass(String owner, String name, String desc) {
		}

		public void visitSource(String source, String debug) {
		}

		public void visitEnd() {
		}

	}

	@Before
	public void setup() {
		adapter = new MockAdapter();
	}

	@Test
	public void testNonAbstractMethods() {
		adapter.visitMethod(ACC_PUBLIC, "a", "()V", null, null);
		adapter.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, "x", "()V", null,
				null);
		adapter.visitMethod(ACC_PUBLIC, "b", "()V", null, null);
		adapter.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, "y", "()V", null,
				null);
		adapter.visitMethod(ACC_PUBLIC, "c", "()V", null, null);
		adapter.assertNonAbstractMethods("a", "b", "c");
	}

	@Test
	public void testAbstractMethods() {
		adapter.visitMethod(ACC_PUBLIC, "a", "()V", null, null);
		adapter.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, "x", "()V", null,
				null);
		adapter.visitMethod(ACC_PUBLIC, "b", "()V", null, null);
		adapter.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, "y", "()V", null,
				null);
		adapter.visitMethod(ACC_PUBLIC, "c", "()V", null, null);
		adapter.assertAbstractMethods("x", "y");
	}

}
