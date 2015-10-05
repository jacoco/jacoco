/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc Pawlowsky - TeatAsCovered annotation.
 *    
 *******************************************************************************/

package org.jacoco.core.internal.annotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.jacoco.annotations.TreatAsCovered;
import org.junit.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class AnnotationsTest {

	private boolean treatAsCoveredFound = false;

	/** Get the class file for this class. */
	private InputStream getThisClassAsStream() throws Exception {
		String fileName = this.getClass().getName().replaceAll("\\.", "/")
				+ ".class";
		InputStream stream = this.getClass().getClassLoader()
				.getResourceAsStream(fileName);
		return stream;
	}

	private class MyMethodVisitor extends MethodVisitor {
		public MyMethodVisitor() {
			super(Opcodes.ASM5);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			System.out.println("visiting annotation " + desc);
			AnnotationsTest.this.treatAsCoveredFound = AnnotationsTest.this.treatAsCoveredFound
					|| Annotations.isTreatAsCovered(desc);
			return super.visitAnnotation(desc, visible);
		}

	}

	private class MyClassVisitor extends ClassVisitor {

		private final String methodToExamine;

		public MyClassVisitor(String methodName) {
			super(Opcodes.ASM5);
			this.methodToExamine = methodName;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			System.out.println("visitMethod name=" + name);
			if (name.contains(this.methodToExamine)) {
				System.out.println("Going to MyMethodVistor");
				return new MyMethodVisitor();
			} else {
				return new MethodVisitor(Opcodes.ASM5) {
				};
			}
		}

	}

	private void visitClass(String methodName) throws Exception {
		InputStream stream = this.getThisClassAsStream();
		ClassReader classReader = new ClassReader(stream);
		ClassVisitor classVisitor = new MyClassVisitor(methodName);
		classReader.accept(classVisitor, 0);
	}

	@Test
	public void testIsNotTreatAsCovered() throws Exception {
		visitClass("testIsNotTreatAsCovered");
		assertFalse(this.treatAsCoveredFound);
	}

	@TreatAsCovered
	@Test
	public void testIsTreatAsCovered() throws Exception {
		visitClass("testIsTreatAsCovered");
		assertTrue(this.treatAsCoveredFound);
	}

}
