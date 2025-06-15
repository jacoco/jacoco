/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.jacoco.core.test.TargetLoader;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link Instrumenter} in method-only mode.
 */
public class MethodOnlyInstrumenterTest {

	private static final class AccessorGenerator
			implements IExecutionDataAccessorGenerator {

		int probeCount = -1;

		public int generateDataAccessor(final long classId,
				final String classname, final int probeCount,
				final MethodVisitor mv) {
			this.probeCount = probeCount;
			InstrSupport.push(mv, probeCount);
			mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
			return 1;
		}
	}

	private AccessorGenerator accessorGenerator;
	private Instrumenter methodOnlyInstrumenter;
	private Instrumenter fullInstrumenter;

	@Before
	public void setup() throws Exception {
		accessorGenerator = new AccessorGenerator();
		methodOnlyInstrumenter = new Instrumenter(accessorGenerator, true);
		fullInstrumenter = new Instrumenter(accessorGenerator, false);
	}

	@Test
	public void testMethodOnlyMarkerAnnotation() throws Exception {
		final byte[] original = TargetLoader
				.getClassDataAsBytes(MethodOnlyInstrumenterTest.Sample.class);
		final byte[] instrumented = methodOnlyInstrumenter.instrument(original,
				"Sample");

		// Verify the instrumented class has the marker annotation
		final ClassReader reader = new ClassReader(instrumented);
		final boolean[] hasMarkerAnnotation = new boolean[1];
		reader.accept(new ClassVisitor(InstrSupport.ASM_API_VERSION) {
			@Override
			public org.objectweb.asm.AnnotationVisitor visitAnnotation(
					final String desc, final boolean visible) {
				if ("Lorg/jacoco/core/internal/instr/JaCoCoMethodOnlyInstrumented;"
						.equals(desc)) {
					hasMarkerAnnotation[0] = true;
				}
				return null;
			}
		}, ClassReader.SKIP_CODE);

		assertTrue("Instrumented class should have marker annotation",
				hasMarkerAnnotation[0]);
	}

	@Test
	public void testMethodOnlyInstrumentationWorks() throws Exception {
		// Test that method-only instrumentation works without errors
		final byte[] original = TargetLoader
				.getClassDataAsBytes(MethodOnlyInstrumenterTest.Sample.class);

		// Method-only instrumentation should succeed
		final byte[] methodOnlyInstrumented = methodOnlyInstrumenter
				.instrument(original, "Sample");

		// Should be able to load the instrumented class
		final TargetLoader loader = new TargetLoader();
		final Class<?> clazz = loader.add(Sample.class.getName(),
				methodOnlyInstrumented);
		assertEquals("org.jacoco.core.instr.MethodOnlyInstrumenterTest$Sample",
				clazz.getName());
	}

	/**
	 * Sample class for testing method-only instrumentation.
	 */
	public static class Sample {
		public void method1() {
			System.out.println("Method 1");
		}

		public void method2() {
			System.out.println("Method 2");
		}

		public void method3() {
			System.out.println("Method 3");
		}
	}

}
