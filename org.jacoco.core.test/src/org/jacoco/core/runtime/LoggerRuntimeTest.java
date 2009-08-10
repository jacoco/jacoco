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

import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.data.IExecutionDataVisitor;
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
 * Unit tests for {@link LoggerRuntime}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class LoggerRuntimeTest {

	private IRuntime runtime;

	private TestStorage storage;

	@Before
	public void setup() {
		runtime = new LoggerRuntime();
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
	public void testCollect1() throws InstantiationException,
			IllegalAccessException {
		final boolean[][] data1 = new boolean[3][];
		generateAndInstantiateClass(1001, data1);
		runtime.collect(storage, false);
		storage.assertSize(1);
		storage.assertData(1001, data1);
	}

	@Test
	public void testCollect2() throws InstantiationException,
			IllegalAccessException {
		final boolean[][] data1 = new boolean[3][];
		final boolean[][] data2 = new boolean[5][];
		generateAndInstantiateClass(1001, data1);
		generateAndInstantiateClass(1002, data2);
		runtime.collect(storage, false);
		storage.assertSize(2);
		storage.assertData(1001, data1);
		storage.assertData(1002, data2);
	}

	@Test
	public void testReset() throws InstantiationException,
			IllegalAccessException {
		final boolean[][] data1 = new boolean[1][];
		data1[0] = new boolean[] { true, true, true };
		generateAndInstantiateClass(1001, data1);
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
		generateAndInstantiateClass(1001, data1);
		runtime.collect(storage, true);
		storage.assertSize(1);
		storage.assertData(1001, data1);
		assertFalse(data1[0][0]);
		assertFalse(data1[0][1]);
		assertFalse(data1[0][2]);
	}

	/**
	 * Creates a new class with the given id, loads this class and injects the
	 * given data instance into the class. Used to check whether the data is
	 * properly collected in the runtime.
	 * 
	 * @param classId
	 * @param data
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void generateAndInstantiateClass(int classId, boolean[][] data)
			throws InstantiationException, IllegalAccessException {

		final String className = "org/jacoco/test/targets/LoggerRuntimeTestTarget";
		final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, className, null,
				"java/lang/Object", new String[] { Type
						.getInternalName(IWriteAccess.class) });

		// Constructor
		GeneratorAdapter gen = new GeneratorAdapter(writer.visitMethod(
				Opcodes.ACC_PUBLIC, "<init>", "()V", null, new String[0]),
				Opcodes.ACC_PUBLIC, "<init>", "()V");
		gen.visitCode();
		gen.loadThis();
		gen.invokeConstructor(Type.getType(Object.class), new Method("<init>",
				"()V"));
		gen.returnValue();
		gen.visitMaxs(0, 0);
		gen.visitEnd();
		writer.visitEnd();

		// set()
		gen = new GeneratorAdapter(writer.visitMethod(Opcodes.ACC_PUBLIC,
				"set", "([[Z)V", null, new String[0]), Opcodes.ACC_PUBLIC,
				"set", "([[Z)V");
		gen.visitCode();
		gen.loadArg(0);
		runtime.generateRegistration(classId, gen);
		gen.returnValue();
		gen.visitMaxs(0, 0);
		gen.visitEnd();
		writer.visitEnd();

		final TargetLoader loader = new TargetLoader(className
				.replace('/', '.'), writer.toByteArray());
		((IWriteAccess) loader.newTargetInstance()).set(data);
	}

	/**
	 * With this interface we inject sample coverage data into the generated
	 * classes.
	 */
	public interface IWriteAccess {

		void set(boolean[][] data);

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

		public void visitClassExecution(long id, boolean[][] blockdata) {
			data.put(Long.valueOf(id), blockdata);
		}

	}

}
