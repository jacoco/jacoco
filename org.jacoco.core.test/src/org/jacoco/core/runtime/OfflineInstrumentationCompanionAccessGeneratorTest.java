/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.runtime;

import org.jacoco.core.internal.instr.Companions;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.test.TargetLoader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Unit tests for {@link OfflineInstrumentationCompanionAccessGenerator}.
 */
public class OfflineInstrumentationCompanionAccessGeneratorTest {

	private static boolean[] probes;

	private OfflineInstrumentationCompanionAccessGenerator generator;

	// runtime stub
	@SuppressWarnings("unused")
	public static boolean[] getProbes(final long classid,
			final String classname, final int probecount) {
		return probes;
	}

	@BeforeClass
	public static void setupClass() {
		probes = new boolean[3];
	}

	@Before
	public void setup() {
		String name = getClass().getName().replace('.', '/');
		generator = new OfflineInstrumentationCompanionAccessGenerator(name);
	}

	@Test
	public void testRuntimeAccess() throws Exception {
		final TargetLoader targetLoader = new TargetLoader();
		final ITarget target = (ITarget) targetLoader.add("Target0", create(0))
				.newInstance();
		targetLoader.add(generator.getClassName(),
				generator.getClassDefinition());
		assertSame(probes, target.get());
	}

	@Test
	public void testLimit() throws Exception {
		for (int i = 0; i < Companions.FIELDS_PER_CLASS; i++) {
			create(i);
		}
		assertEquals(Companions.FIELDS_PER_CLASS,
				generator.getNumberOfInstrumentedClasses());
		new TargetLoader().add(generator.getClassName(),
				generator.getClassDefinition());
		assertEquals(0, generator.getNumberOfInstrumentedClasses());
	}

	@Test
	public void testFieldName() throws Exception {
		assertEquals("pffffffffffffffff",
				OfflineInstrumentationCompanionAccessGenerator
						.fieldNameFor(-1));
	}

	private byte[] create(final int classId)
			throws IllegalAccessException, InstantiationException {
		final String className = "Target" + classId;
		final ClassWriter writer = new ClassWriter(0);
		writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, className, null,
				"java/lang/Object",
				new String[] { Type.getInternalName(ITarget.class) });

		// Constructor
		GeneratorAdapter gen = new GeneratorAdapter(
				writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null,
						new String[0]),
				Opcodes.ACC_PUBLIC, "<init>", "()V");
		gen.visitCode();
		gen.loadThis();
		gen.invokeConstructor(Type.getType(Object.class),
				new Method("<init>", "()V"));
		gen.returnValue();
		gen.visitMaxs(1, 0);
		gen.visitEnd();

		// get()
		gen = new GeneratorAdapter(
				writer.visitMethod(Opcodes.ACC_PUBLIC, "get",
						InstrSupport.INITMETHOD_DESC, null, new String[0]),
				Opcodes.ACC_PUBLIC, "get", InstrSupport.INITMETHOD_DESC);
		gen.visitCode();
		generator.generateDataAccessor(classId, className, 3, gen);
		gen.returnValue();
		gen.visitMaxs(2, 0);
		gen.visitEnd();

		writer.visitEnd();

		return writer.toByteArray();
	}

	public interface ITarget {

		/**
		 * Returns a reference to the probe array.
		 *
		 * @return the probe array
		 */
		boolean[] get();

	}

}
