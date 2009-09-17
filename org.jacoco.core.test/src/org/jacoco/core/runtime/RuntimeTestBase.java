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
package org.jacoco.core.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.instr.GeneratorConstants;
import org.jacoco.core.test.TargetLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

/**
 * Abstract test base for {@link IRuntime} implementations.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public abstract class RuntimeTestBase {

	private IRuntime runtime;

	private TestStorage storage;

	abstract IRuntime createRuntime();

	@Before
	public void setup() {
		runtime = createRuntime();
		runtime.startup();
		storage = new TestStorage();
	}

	@After
	public void shutdown() {
		runtime.shutdown();
	}

	@Test
	public void testCollectEmpty() {
		runtime.collect(storage, false);
		storage.assertSize(0);
	}

	@Test
	public void testReset() throws InstantiationException,
			IllegalAccessException {
		final boolean[][] data1 = new boolean[1][];
		data1[0] = new boolean[] { true, true, true };
		runtime.registerClass(1001, "Target", data1);
		runtime.reset();
		assertFalse(data1[0][0]);
		assertFalse(data1[0][1]);
		assertFalse(data1[0][2]);
	}

	@Test
	public void testCollectAndReset() throws InstantiationException,
			IllegalAccessException {
		final boolean[][] data1 = new boolean[1][];
		data1[0] = new boolean[] { true, true, true };
		runtime.registerClass(1001, "Target", data1);
		runtime.collect(storage, true);
		storage.assertSize(1);
		storage.assertData(1001, data1);
		assertFalse(data1[0][0]);
		assertFalse(data1[0][1]);
		assertFalse(data1[0][2]);
	}

	@Test(expected = IllegalStateException.class)
	public void testNoClassRegistration() throws InstantiationException,
			IllegalAccessException {
		generateAndInstantiateClass(1001);
	}

	@Test
	public void testDataAccessor() throws InstantiationException,
			IllegalAccessException {
		final boolean[][] data = newStructure();
		runtime.registerClass(1001, "Target", data);
		ITarget t = generateAndInstantiateClass(1001);
		assertSame(data, t.get());
	}

	@Test
	public void testExecutionRecording() throws InstantiationException,
			IllegalAccessException {
		boolean[][] data1 = newStructure();
		runtime.registerClass(1001, "Target", data1);
		generateAndInstantiateClass(1001).a();
		runtime.collect(storage, false);
		storage.assertSize(1);
		storage.assertData(1001, data1);
		assertTrue(data1[0][0]);
		assertFalse(data1[1][0]);
	}

	@Test
	public void testLoadSameClassTwice() throws InstantiationException,
			IllegalAccessException {
		boolean[][] data1 = newStructure();
		runtime.registerClass(1001, "Target", data1);
		generateAndInstantiateClass(1001).a();
		generateAndInstantiateClass(1001).b();
		runtime.collect(storage, false);
		storage.assertSize(1);
		storage.assertData(1001, data1);
		assertTrue(data1[0][0]);
		assertTrue(data1[1][0]);
	}

	/**
	 * Creates a new class with the given id, loads this class and injects the
	 * given data instance into the class. Used to check whether the data is
	 * properly collected in the runtime.
	 * 
	 * @param classid
	 * @param data
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private ITarget generateAndInstantiateClass(int classid)
			throws InstantiationException, IllegalAccessException {

		final String className = "org/jacoco/test/targets/RuntimeTestTarget_"
				+ classid;
		Type classType = Type.getObjectType(className);

		final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, className, null,
				"java/lang/Object", new String[] { Type
						.getInternalName(ITarget.class) });

		writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "data",
				"[[Z", null, null);

		// Constructor
		GeneratorAdapter gen = new GeneratorAdapter(writer.visitMethod(
				Opcodes.ACC_PUBLIC, "<init>", "()V", null, new String[0]),
				Opcodes.ACC_PUBLIC, "<init>", "()V");
		gen.visitCode();
		gen.loadThis();
		gen.invokeConstructor(Type.getType(Object.class), new Method("<init>",
				"()V"));
		gen.loadThis();
		runtime.generateDataAccessor(classid, gen);
		gen.putField(classType, "data", GeneratorConstants.DATAFIELD_TYPE);
		gen.returnValue();
		gen.visitMaxs(0, 0);
		gen.visitEnd();

		// get()
		gen = new GeneratorAdapter(writer.visitMethod(Opcodes.ACC_PUBLIC,
				"get", "()[[Z", null, new String[0]), Opcodes.ACC_PUBLIC,
				"get", "()[[Z");
		gen.visitCode();
		gen.loadThis();
		gen.getField(classType, "data", GeneratorConstants.DATAFIELD_TYPE);
		gen.returnValue();
		gen.visitMaxs(0, 0);
		gen.visitEnd();

		// a()
		gen = new GeneratorAdapter(writer.visitMethod(Opcodes.ACC_PUBLIC, "a",
				"()V", null, new String[0]), Opcodes.ACC_PUBLIC, "a", "()V");
		gen.visitCode();
		gen.loadThis();
		gen.getField(classType, "data", GeneratorConstants.DATAFIELD_TYPE);
		gen.push(0);
		gen.arrayLoad(Type.getObjectType("[Z"));
		gen.push(0);
		gen.push(1);
		gen.arrayStore(Type.BOOLEAN_TYPE);
		gen.returnValue();
		gen.visitMaxs(0, 0);
		gen.visitEnd();

		// a()
		gen = new GeneratorAdapter(writer.visitMethod(Opcodes.ACC_PUBLIC, "b",
				"()V", null, new String[0]), Opcodes.ACC_PUBLIC, "b", "()V");
		gen.visitCode();
		gen.loadThis();
		gen.getField(classType, "data", GeneratorConstants.DATAFIELD_TYPE);
		gen.push(1);
		gen.arrayLoad(Type.getObjectType("[Z"));
		gen.push(0);
		gen.push(1);
		gen.arrayStore(Type.BOOLEAN_TYPE);
		gen.returnValue();
		gen.visitMaxs(0, 0);
		gen.visitEnd();

		writer.visitEnd();

		final TargetLoader loader = new TargetLoader(className
				.replace('/', '.'), writer.toByteArray());
		return (ITarget) loader.newTargetInstance();
	}

	/**
	 * With this interface we inject sample coverage data into the generated
	 * classes.
	 */
	public interface ITarget {

		boolean[][] get();

		// implementations just mark method 0 as executed
		void a();

		// implementations just mark method 1 as executed
		void b();

	}

	private boolean[][] newStructure() {
		return new boolean[][] { new boolean[] { false },
				new boolean[] { false } };
	}

	private static class TestStorage implements IExecutionDataVisitor {

		private final Map<Long, boolean[][]> data = new HashMap<Long, boolean[][]>();

		public void assertSize(int size) {
			assertEquals(size, data.size(), 0.0);
		}

		public boolean[][] getData(long classId) {
			return data.get(Long.valueOf(classId));
		}

		public void assertData(long classId, boolean[][] expected) {
			assertSame(expected, getData(classId));
		}

		// === ICoverageDataVisitor ===

		public void visitClassExecution(long id, String name,
				boolean[][] blockdata) {
			data.put(Long.valueOf(id), blockdata);
		}

	}

}
