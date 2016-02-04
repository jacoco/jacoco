/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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

import java.util.concurrent.atomic.AtomicIntegerArray;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.data.ProbeMode;
import org.jacoco.core.instr.MethodRecorder;
import org.jacoco.core.internal.instr.IProbeArray;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.internal.instr.ProbeArrayService;
import org.jacoco.core.internal.instr.ProbeDoubleIntArray;
import org.jacoco.core.test.TargetLoader;
import org.junit.AfterClass;
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
	private static transient IProbeArray<?> probes;

	public static class OfflineInstrumentationAccessGeneratorExistsTest extends
			OfflineInstrumentationAccessGeneratorTestBase<boolean[]> {
		@BeforeClass
		public static void setupClass() {
			ProbeArrayService.reset();
			ProbeArrayService.configure(ProbeMode.exists);
			probes = ProbeArrayService.newProbeArray(3);
		}

		@AfterClass
		public static void cleanupProbeMode() {
			ProbeArrayService.reset();
		}
	}

	public static class OfflineInstrumentationAccessGeneratorCountTest extends
			OfflineInstrumentationAccessGeneratorTestBase<AtomicIntegerArray> {
		@BeforeClass
		public static void setupClass() {
			ProbeArrayService.reset();
			ProbeArrayService.configure(ProbeMode.count);
			probes = ProbeArrayService.newProbeArray(3);
		}

		@AfterClass
		public static void cleanupProbeMode() {
			ProbeArrayService.reset();
		}
	}

	public static class OfflineInstrumentationAccessGeneratorParallelTest
			extends
			OfflineInstrumentationAccessGeneratorTestBase<ProbeDoubleIntArray> {
		@BeforeClass
		public static void setupClass() {
			ProbeArrayService.reset();
			ProbeArrayService.configure(ProbeMode.parallelcount);
			probes = ProbeArrayService.newProbeArray(3);
		}

		@AfterClass
		public static void cleanupProbeMode() {
			ProbeArrayService.reset();
		}
	}

	public static abstract class OfflineInstrumentationAccessGeneratorTestBase<T> {

		private IExecutionDataAccessorGenerator generator;

		// runtime stub
		public static Object getProbesObject(final long classid,
				final String classname, final int probecount) {
			return probes.getProbesObject();
		}

		@Before
		public void setup() {
			String name = getClass().getName().replace('.', '/');
			generator = new OfflineInstrumentationAccessGenerator(name);
		}

		@Test
		public void testRuntimeAccess() throws Exception {
			ITarget<T> target = generateAndInstantiateClass(123);
			assertEquals(probes.getProbesObject(), target.get());
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
			String rtname = JaCoCo.RUNTIMEPACKAGE.replace('.', '/')
					+ "/Offline";
			expected.getVisitor().visitMethodInsn(Opcodes.INVOKESTATIC, rtname,
					"getProbesObject",
					"(JLjava/lang/String;I)Ljava/lang/Object;", false);
			expected.getVisitor().visitTypeInsn(Opcodes.CHECKCAST,
					ProbeArrayService.getDatafieldClass());

			assertEquals(expected, actual);
		}

		/**
		 * Creates a new class with the given id, loads this class and
		 * instantiates it. The constructor of the generated class will request
		 * the probe array from the access generator under test.
		 */
		private ITarget<T> generateAndInstantiateClass(int classid)
				throws InstantiationException, IllegalAccessException {

			final String className = "org/jacoco/test/targets/RuntimeTestTarget_"
					+ classid;
			Type classType = Type.getObjectType(className);

			final ClassWriter writer = new ClassWriter(0);
			writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, className, null,
					"java/lang/Object",
					new String[] { Type.getInternalName(ITarget.class) });

			writer.visitField(InstrSupport.DATAFIELD_ACC,
					InstrSupport.DATAFIELD_NAME,
					ProbeArrayService.getDatafieldDesc(), null, null);

			// Constructor
			GeneratorAdapter gen = new GeneratorAdapter(writer.visitMethod(
					Opcodes.ACC_PUBLIC, "<init>", "()V", null, new String[0]),
					Opcodes.ACC_PUBLIC, "<init>", "()V");
			gen.visitCode();
			gen.loadThis();
			gen.invokeConstructor(Type.getType(Object.class), new Method(
					"<init>", "()V"));
			gen.loadThis();
			final int size = generator.generateDataAccessor(classid, className,
					2, gen);
			gen.putStatic(classType, InstrSupport.DATAFIELD_NAME,
					Type.getObjectType(ProbeArrayService.getDatafieldClass()));
			gen.returnValue();
			gen.visitMaxs(size + 1, 0);
			gen.visitEnd();

			// T getInternal()
			gen = new GeneratorAdapter(writer.visitMethod(Opcodes.ACC_PUBLIC,
					"getInternal", "()" + ProbeArrayService.getDatafieldDesc(),
					null, new String[0]), Opcodes.ACC_PUBLIC, "getInternal",
					"()" + ProbeArrayService.getDatafieldClass());
			gen.visitCode();
			gen.getStatic(classType, InstrSupport.DATAFIELD_NAME,
					Type.getObjectType(ProbeArrayService.getDatafieldClass()));
			gen.returnValue();
			gen.visitMaxs(1, 0);
			gen.visitEnd();

			// Object get()
			gen = new GeneratorAdapter(writer.visitMethod(Opcodes.ACC_PUBLIC,
					"get", "()Ljava/lang/Object;", null, new String[0]),
					Opcodes.ACC_PUBLIC, "get", "()Ljava/lang/Object;");
			gen.visitCode();
			gen.getStatic(classType, InstrSupport.DATAFIELD_NAME,
					Type.getObjectType(ProbeArrayService.getDatafieldClass()));
			gen.returnValue();
			gen.visitMaxs(1, 0);
			gen.visitEnd();

			writer.visitEnd();

			final TargetLoader loader = new TargetLoader();
			@SuppressWarnings("unchecked")
			ITarget<T> targetClass = (ITarget<T>) loader.add(
					className.replace('/', '.'), writer.toByteArray())
					.newInstance();
			return targetClass;
		}

		/**
		 * With this interface access read coverage data of the generated class.
		 */
		public interface ITarget<T> {

			/**
			 * Returns a reference to the probe array.
			 * 
			 * @return the probe array
			 */
			T get();

		}
	}
}
