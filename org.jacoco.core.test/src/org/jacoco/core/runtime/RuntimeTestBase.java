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
 *******************************************************************************/
package org.jacoco.core.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.test.TargetLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

/**
 * Abstract test base for {@link IRuntime} implementations.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public abstract class RuntimeTestBase {

	private IRuntime runtime;

	private TestStorage storage;

	abstract IRuntime createRuntime();

	@Before
	public void setup() throws Exception {
		runtime = createRuntime();
		runtime.startup();
		storage = new TestStorage();
	}

	@After
	public void shutdown() {
		runtime.shutdown();
	}

	@Test
	public void testGetSetSessionId() {
		assertNotNull(runtime.getSessionId());
		runtime.setSessionId("test-id");
		assertEquals("test-id", runtime.getSessionId());
	}

	@Test
	public void testCollectEmpty() {
		runtime.collect(storage, null, false);
		storage.assertSize(0);
	}

	@Test
	public void testDataAccessor() throws InstantiationException,
			IllegalAccessException {
		ITarget t = generateAndInstantiateClass(1234);
		runtime.collect(storage, null, false);
		storage.assertData(1234, t.get());
	}

	@Test
	public void testReset() throws InstantiationException,
			IllegalAccessException {
		final ITarget target = generateAndInstantiateClass(1000);
		target.a();
		target.b();

		runtime.reset();

		final boolean[] data = target.get();
		assertFalse(data[0]);
		assertFalse(data[1]);
	}

	@Test
	public void testCollectAndReset() throws InstantiationException,
			IllegalAccessException {
		final ITarget target = generateAndInstantiateClass(1001);
		target.a();
		target.b();

		runtime.collect(storage, null, true);

		final boolean[] data = target.get();
		storage.assertSize(1);
		storage.assertData(1001, data);
		assertFalse(data[0]);
		assertFalse(data[1]);
	}

	@Test
	public void testSessionInfo() throws Exception {
		final SessionInfo[] info = new SessionInfo[1];
		final ISessionInfoVisitor visitor = new ISessionInfoVisitor() {
			public void visitSessionInfo(SessionInfo i) {
				info[0] = i;
			}
		};
		runtime.setSessionId("test-session");
		final long t1 = System.currentTimeMillis();
		runtime.startup();
		runtime.collect(storage, visitor, true);
		final long t2 = System.currentTimeMillis();
		assertNotNull(info[0]);
		assertEquals("test-session", info[0].getId());
		assertTrue(info[0].getStartTimeStamp() >= t1);
		assertTrue(info[0].getDumpTimeStamp() <= t2);

		info[0] = null;
		runtime.collect(storage, visitor, true);
		final long t3 = System.currentTimeMillis();
		assertNotNull(info[0]);
		assertEquals("test-session", info[0].getId());
		assertTrue(info[0].getStartTimeStamp() >= t2);
		assertTrue(info[0].getDumpTimeStamp() <= t3);
	}

	@Test
	public void testNoLocalVariablesInDataAccessor()
			throws InstantiationException, IllegalAccessException {
		runtime.generateDataAccessor(1001, "Target", 5, new EmptyVisitor() {
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
		runtime.collect(storage, null, false);
		storage.assertSize(1);
		final boolean[] data = storage.getData(1001);
		assertTrue(data[0]);
		assertFalse(data[1]);
	}

	@Test
	public void testLoadSameClassTwice() throws InstantiationException,
			IllegalAccessException {
		generateAndInstantiateClass(1001).a();
		generateAndInstantiateClass(1001).b();
		runtime.collect(storage, null, false);
		storage.assertSize(1);
		final boolean[] data = storage.getData(1001);
		assertTrue(data[0]);
		assertTrue(data[1]);
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

		final ClassWriter writer = new ClassWriter(0);
		writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, className, null,
				"java/lang/Object",
				new String[] { Type.getInternalName(ITarget.class) });

		writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "data",
				"[Z", null, null);

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
		gen.putField(classType, "data", Type.getObjectType("[Z"));
		gen.returnValue();
		gen.visitMaxs(size + 1, 0);
		gen.visitEnd();

		// get()
		gen = new GeneratorAdapter(writer.visitMethod(Opcodes.ACC_PUBLIC,
				"get", "()[Z", null, new String[0]), Opcodes.ACC_PUBLIC, "get",
				"()[Z");
		gen.visitCode();
		gen.loadThis();
		gen.getField(classType, "data", Type.getObjectType("[Z"));
		gen.returnValue();
		gen.visitMaxs(1, 0);
		gen.visitEnd();

		// a()
		gen = new GeneratorAdapter(writer.visitMethod(Opcodes.ACC_PUBLIC, "a",
				"()V", null, new String[0]), Opcodes.ACC_PUBLIC, "a", "()V");
		gen.visitCode();
		gen.loadThis();
		gen.getField(classType, "data", Type.getObjectType("[Z"));
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
		gen.loadThis();
		gen.getField(classType, "data", Type.getObjectType("[Z"));
		gen.push(1);
		gen.push(1);
		gen.arrayStore(Type.BOOLEAN_TYPE);
		gen.returnValue();
		gen.visitMaxs(3, 0);
		gen.visitEnd();

		writer.visitEnd();

		final TargetLoader loader = new TargetLoader(
				className.replace('/', '.'), writer.toByteArray());
		return (ITarget) loader.newTargetInstance();
	}

	/**
	 * With this interface we inject sample coverage data into the generated
	 * classes.
	 */
	public interface ITarget {

		boolean[] get();

		// implementations just mark probe 0 as executed
		void a();

		// implementations just mark probe 1 as executed
		void b();

	}

	private static class TestStorage implements IExecutionDataVisitor {

		private final Map<Long, boolean[]> data = new HashMap<Long, boolean[]>();

		public void assertSize(int size) {
			assertEquals(size, data.size(), 0.0);
		}

		public boolean[] getData(long classId) {
			return data.get(Long.valueOf(classId));
		}

		public void assertData(long classId, boolean[] expected) {
			assertSame(expected, getData(classId));
		}

		// === ICoverageDataVisitor ===

		public void visitClassExecution(final ExecutionData ed) {
			data.put(Long.valueOf(ed.getId()), ed.getData());
		}

	}

}
