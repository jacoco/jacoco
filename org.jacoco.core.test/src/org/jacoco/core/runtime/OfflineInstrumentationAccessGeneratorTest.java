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
package org.jacoco.core.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.instr.MethodRecorder;
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

/**
 * Unit tests for {@link OfflineInstrumentationAccessGenerator}.
 */
public class OfflineInstrumentationAccessGeneratorTest {

	private IExecutionDataAccessorGenerator generator;

	private static boolean[] probes;

	// runtime stub
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
		generator = new OfflineInstrumentationAccessGenerator(name);
	}

	@Test
	public void testRuntimeAccess() throws Exception {
		ITarget target = generateAndInstantiateClass(123);
		assertSame(probes, target.get());
	}

	@Test
	public void testRuntimeClassName() throws Exception {
		generator = new OfflineInstrumentationAccessGenerator();
		MethodRecorder actual = new MethodRecorder();
		generator.generateDataAccessor(987654321, "foo/Bar", 17,
				actual.getVisitor());

		MethodRecorder expected = new MethodRecorder();
		expected.getVisitor().visitLdcInsn(Long.valueOf(987654321));
		expected.getVisitor().visitLdcInsn("foo/Bar");
		expected.getVisitor().visitIntInsn(Opcodes.BIPUSH, 17);
		String rtname = JaCoCo.RUNTIMEPACKAGE.replace('.', '/') + "/Offline";
		expected.getVisitor().visitMethodInsn(Opcodes.INVOKESTATIC, rtname,
				"getProbes", "(JLjava/lang/String;I)[Z", false);

		assertEquals(expected, actual);
	}

	/**
	 * Creates a new class with the given id, loads this class and instantiates
	 * it. The constructor of the generated class will request the probe array
	 * from the access generator under test.
	 */
	private ITarget generateAndInstantiateClass(int classid)
			throws InstantiationException, IllegalAccessException {

		final String className = "org/jacoco/test/targets/RuntimeTestTarget_"
				+ classid;
		Type classType = Type.getObjectType(className);

		final ClassWriter writer = new ClassWriter(0);
		writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, className, null,
				"java/lang/Object",
				new String[] { Type.getInternalName(ITarget.class) });

		writer.visitField(InstrSupport.DATAFIELD_ACC,
				InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC, null,
				null);

		// Constructor
		GeneratorAdapter gen = new GeneratorAdapter(
				writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null,
						new String[0]),
				Opcodes.ACC_PUBLIC, "<init>", "()V");
		gen.visitCode();
		gen.loadThis();
		gen.invokeConstructor(Type.getType(Object.class),
				new Method("<init>", "()V"));
		gen.loadThis();
		final int size = generator.generateDataAccessor(classid, className, 2,
				gen);
		gen.putStatic(classType, InstrSupport.DATAFIELD_NAME,
				Type.getObjectType(InstrSupport.DATAFIELD_DESC));
		gen.returnValue();
		gen.visitMaxs(size + 1, 0);
		gen.visitEnd();

		// get()
		gen = new GeneratorAdapter(writer.visitMethod(Opcodes.ACC_PUBLIC, "get",
				"()[Z", null, new String[0]), Opcodes.ACC_PUBLIC, "get",
				"()[Z");
		gen.visitCode();
		gen.getStatic(classType, InstrSupport.DATAFIELD_NAME,
				Type.getObjectType(InstrSupport.DATAFIELD_DESC));
		gen.returnValue();
		gen.visitMaxs(1, 0);
		gen.visitEnd();

		writer.visitEnd();

		final TargetLoader loader = new TargetLoader();
		return (ITarget) loader
				.add(className.replace('/', '.'), writer.toByteArray())
				.newInstance();
	}

	/**
	 * With this interface access read coverage data of the generated class.
	 */
	public interface ITarget {

		/**
		 * Returns a reference to the probe array.
		 *
		 * @return the probe array
		 */
		boolean[] get();

	}

}
