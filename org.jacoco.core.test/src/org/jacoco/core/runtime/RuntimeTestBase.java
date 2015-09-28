/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.runtime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.internal.instr.IProbeArray;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.internal.instr.ProbeArrayService;
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
public abstract class RuntimeTestBase<T> {

	private RuntimeData data;

	private IRuntime runtime;

	private TestStorage<T> storage;

	abstract IRuntime createRuntime();

	@Before
	public void setup() throws Exception {
		data = new RuntimeData();
		runtime = createRuntime();
		runtime.startup(data);
		storage = new TestStorage<T>();
	}

	@After
	public void shutdown() {
		runtime.shutdown();
	}

	@Test
	public void testDataAccessor() throws InstantiationException,
			IllegalAccessException {
		ITarget<T> t = generateAndInstantiateClass(1234);
		data.collect(storage, storage, false);
		storage.assertData(1234, t.get());
	}

	@Test
	public void testNoLocalVariablesInDataAccessor()
			throws InstantiationException, IllegalAccessException {
		runtime.generateDataAccessor(1001, "Target", 5, new MethodVisitor(
				JaCoCo.ASM_API_VERSION) {
			@Override
			public void visitVarInsn(int opcode, int var) {
				fail("No usage of local variables allowed.");
			}
		});
	}

	@Test
	public void testExecutionRecording() throws InstantiationException,
			IllegalAccessException {
		generateAndInstantiateClass(1001).a();
		data.collect(storage, storage, false);
		storage.assertSize(1);
		final IProbeArray<?> data = storage.getData(1001).getProbes();
		assertTrue(data.isProbeCovered(0));
		assertFalse(data.isProbeCovered(1));
	}

	@Test
	public void testLoadSameClassTwice() throws InstantiationException,
			IllegalAccessException {
		generateAndInstantiateClass(1001).a();
		generateAndInstantiateClass(1001).b();
		data.collect(storage, storage, false);
		storage.assertSize(1);
		final IProbeArray<?> data = storage.getData(1001).getProbes();
		assertTrue(data.isProbeCovered(0));
		assertTrue(data.isProbeCovered(1));
	}

	/**
	 * Creates a new class with the given id, loads this class and instantiates
	 * it. The constructor of the generated class will request the probe array
	 * from the runtime under test.
	 */
	private ITarget<T> generateAndInstantiateClass(int classid)
			throws InstantiationException, IllegalAccessException {

		final String className = "org/jacoco/test/targets/RuntimeTestTarget_"
				+ classid;
		Type classType = Type.getObjectType(className);
		String dataFieldClass = ProbeArrayService.getDatafieldClass();
		String dataFieldDesc = ProbeArrayService.getDatafieldDesc();

		final ClassWriter writer = new ClassWriter(0);
		writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, className, null,
				"java/lang/Object",
				new String[] { Type.getInternalName(ITarget.class) });

		writer.visitField(InstrSupport.DATAFIELD_ACC,
				InstrSupport.DATAFIELD_NAME, dataFieldDesc, null, null);

		// Constructor
		GeneratorAdapter gen = new GeneratorAdapter(writer.visitMethod(
				Opcodes.ACC_PUBLIC, "<init>", "()V", null, new String[0]),
				Opcodes.ACC_PUBLIC, "<init>", "()V");
		gen.visitCode();
		gen.loadThis();
		gen.invokeConstructor(Type.getType(Object.class), new Method("<init>",
				"()V"));
		gen.loadThis();
		final int size = runtime.generateDataAccessor(classid, className, 2,
				gen);
		gen.putStatic(classType, InstrSupport.DATAFIELD_NAME,
				Type.getObjectType(dataFieldClass));
		gen.returnValue();
		gen.visitMaxs(size + 1, 0);
		gen.visitEnd();

		// T getInternal()
		gen = new GeneratorAdapter(writer.visitMethod(Opcodes.ACC_PUBLIC,
				"getInternal", "()" + ProbeArrayService.getDatafieldDesc(),
				null, new String[0]), Opcodes.ACC_PUBLIC, "getInternal", "()"
				+ dataFieldDesc);
		gen.visitCode();
		gen.getStatic(classType, InstrSupport.DATAFIELD_NAME,
				Type.getObjectType(dataFieldClass));
		gen.returnValue();
		gen.visitMaxs(1, 0);
		gen.visitEnd();

		// Object get()
		gen = new GeneratorAdapter(writer.visitMethod(Opcodes.ACC_PUBLIC,
				"get", "()Ljava/lang/Object;", null, new String[0]),
				Opcodes.ACC_PUBLIC, "get", "()Ljava/lang/Object;");
		gen.visitCode();
		gen.visitVarInsn(Opcodes.ALOAD, 0);
		gen.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, "getInternal",
				"()" + dataFieldDesc, false);

		gen.returnValue();
		gen.visitMaxs(1, 0);
		gen.visitEnd();

		// a()
		gen = new GeneratorAdapter(writer.visitMethod(Opcodes.ACC_PUBLIC, "a",
				"()V", null, new String[0]), Opcodes.ACC_PUBLIC, "a", "()V");
		gen.visitCode();
		gen.getStatic(classType, InstrSupport.DATAFIELD_NAME,
				Type.getObjectType(ProbeArrayService.getDatafieldClass()));
		ProbeArrayService.insertProbe(gen, -1, 0);
		gen.returnValue();
		gen.visitMaxs(4, 0);
		gen.visitEnd();

		// b()
		gen = new GeneratorAdapter(writer.visitMethod(Opcodes.ACC_PUBLIC, "b",
				"()V", null, new String[0]), Opcodes.ACC_PUBLIC, "b", "()V");
		gen.visitCode();
		gen.getStatic(classType, InstrSupport.DATAFIELD_NAME,
				Type.getObjectType(ProbeArrayService.getDatafieldClass()));
		ProbeArrayService.insertProbe(gen, -1, 1);
		gen.returnValue();
		gen.visitMaxs(4, 0);
		gen.visitEnd();

		writer.visitEnd();

		final TargetLoader loader = new TargetLoader();
		@SuppressWarnings("unchecked")
		ITarget<T> target = (ITarget<T>) loader.add(
				className.replace('/', '.'), writer.toByteArray())
				.newInstance();
		return target;
	}

	/**
	 * With this interface we modify and read coverage data of the generated
	 * class.
	 * 
	 * @param <T>
	 *            the same type as the implemented probe array
	 */
	public interface ITarget<T> {

		/**
		 * Returns a reference to the probe array.
		 * 
		 * @return the probe array
		 */
		T get();

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
