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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.test.TargetLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

/**
 * Abstract test base for {@link IRuntime} implementations.
 */
public abstract class RuntimeTestBase {

	private RuntimeData data;

	private IRuntime runtime;

	private TestStorage storage;

	abstract IRuntime createRuntime();

	@Before
	public void setup() throws Exception {
		data = new RuntimeData();
		runtime = createRuntime();
		runtime.startup(data);
		storage = new TestStorage();
	}

	@After
	public void shutdown() {
		runtime.shutdown();
	}

	@Test
	public void testDataAccessor()
			throws InstantiationException, IllegalAccessException {
		ITarget t = generateAndInstantiateClass(1234);
		data.collect(storage, storage, false);
		storage.assertData(1234, t.get());
	}

	@Test
	public void testNoLocalVariablesInDataAccessor()
			throws InstantiationException, IllegalAccessException {
		runtime.generateDataAccessor(1001, "Target", 5,
				new MethodVisitor(InstrSupport.ASM_API_VERSION) {
					@Override
					public void visitVarInsn(int opcode, int var) {
						fail("No usage of local variables allowed.");
					}
				});
	}

	@Test
	public void testExecutionRecording()
			throws InstantiationException, IllegalAccessException {
		generateAndInstantiateClass(1001).a();
		data.collect(storage, storage, false);
		storage.assertSize(1);
		final boolean[] data = storage.getData(1001).getProbes();
		assertTrue(data[0]);
		assertFalse(data[1]);
	}

	@Test
	public void testLoadSameClassTwice()
			throws InstantiationException, IllegalAccessException {
		generateAndInstantiateClass(1001).a();
		generateAndInstantiateClass(1001).b();
		data.collect(storage, storage, false);
		storage.assertSize(1);
		final boolean[] data = storage.getData(1001).getProbes();
		assertTrue(data[0]);
		assertTrue(data[1]);
	}

	/**
	 * Creates a new class with the given id, loads this class and instantiates
	 * it. The constructor of the generated class will request the probe array
	 * from the runtime under test.
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
		final int size = runtime.generateDataAccessor(classid, className, 2,
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

		// a()
		gen = new GeneratorAdapter(writer.visitMethod(Opcodes.ACC_PUBLIC, "a",
				"()V", null, new String[0]), Opcodes.ACC_PUBLIC, "a", "()V");
		gen.visitCode();
		gen.getStatic(classType, InstrSupport.DATAFIELD_NAME,
				Type.getObjectType(InstrSupport.DATAFIELD_DESC));
		gen.push(0);
		gen.push(1);
		gen.arrayStore(Type.BOOLEAN_TYPE);
		gen.returnValue();
		gen.visitMaxs(3, 0);
		gen.visitEnd();

		// b()
		gen = new GeneratorAdapter(writer.visitMethod(Opcodes.ACC_PUBLIC, "b",
				"()V", null, new String[0]), Opcodes.ACC_PUBLIC, "b", "()V");
		gen.visitCode();
		gen.getStatic(classType, InstrSupport.DATAFIELD_NAME,
				Type.getObjectType(InstrSupport.DATAFIELD_DESC));
		gen.push(1);
		gen.push(1);
		gen.arrayStore(Type.BOOLEAN_TYPE);
		gen.returnValue();
		gen.visitMaxs(3, 0);
		gen.visitEnd();

		writer.visitEnd();

		final TargetLoader loader = new TargetLoader();
		return (ITarget) loader
				.add(className.replace('/', '.'), writer.toByteArray())
				.newInstance();
	}

	/**
	 * With this interface we modify and read coverage data of the generated
	 * class.
	 */
	public interface ITarget {

		/**
		 * Returns a reference to the probe array.
		 *
		 * @return the probe array
		 */
		boolean[] get();

		/**
		 * The implementation will mark probe 0 as executed
		 */
		void a();

		/**
		 * The implementation will mark probe 1 as executed
		 */
		void b();

	}

}
