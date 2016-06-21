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
package org.jacoco.core.internal.instr;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

import org.jacoco.core.internal.Java9Support;
import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.runtime.RuntimeData;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Instrumentation with creation of "companion" classes in "online" mode, which
 * will hold state of instrumented classes. This allows to avoid addition of
 * members to classes.
 */
public class Companions {

	/**
	 * {@link ClassLoader#defineClass(String, byte[], int, int)}
	 */
	private static final Method DEFINE_CLASS;

	static {
		try {
			DEFINE_CLASS = ClassLoader.class.getDeclaredMethod("defineClass",
					String.class, byte[].class, Integer.TYPE, Integer.TYPE);
			DEFINE_CLASS.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Prefix for the names of generated classes.
	 */
	public static final String COMPANION_NAME = InstrSupport.DATAFIELD_NAME;

	private static final String INIT_METHOD_NAME = InstrSupport.INITMETHOD_NAME;

	/**
	 * https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.11
	 */
	public static final int FIELDS_PER_CLASS = 5000;

	static class Companion {
		private int lastId = -1;

		// TODO(Godin):
		// we can utilize ReferenceQueue to cleanup unused probe arrays
		// https://github.com/jacoco/jacoco/issues/134
		WeakReference<Class<?>> cls;

		int usedFields;

		void defineNew(final ClassLoader classLoader) {
			usedFields = 0;
			lastId++;
			final byte[] definition = createCompanionClass(getName());
			try {
				cls = new WeakReference<Class<?>>(
						(Class<?>) DEFINE_CLASS.invoke(classLoader, getName(),
								definition, 0, definition.length));
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		String getName() {
			return COMPANION_NAME + lastId;
		}
	}

	private final Map<ClassLoader, Companion> companions = new WeakHashMap<ClassLoader, Companion>();

	private final RuntimeData runtimeData;

	/**
	 * Creates a new instance for "online" instrumentation.
	 * 
	 * @param runtimeData
	 *            execution data
	 */
	public Companions(final RuntimeData runtimeData) {
		this.runtimeData = runtimeData;
	}

	/**
	 * Creates instrumented version of the given class.
	 *
	 * @param classLoader
	 *            defining loader
	 * @param className
	 *            name of the class
	 * @param buffer
	 *            definition of the class
	 * @return instrumented definition
	 */
	public byte[] instrument(final ClassLoader classLoader,
			final String className, final byte[] buffer) {
		final boolean java9 = Java9Support.isPatchRequired(buffer);
		final ClassReader reader = new ClassReader(
				java9 ? Java9Support.downgrade(buffer) : buffer);

		final int probeCount = ProbeArrayStrategyFactory.getProbeCounter(reader)
				.getCount();
		if (probeCount == 0) {
			return buffer;
		}
		final long classId = CRC64.checksum(reader.b);
		final IProbeArrayStrategy strategy = createStrategyFor(classLoader,
				classId, className, probeCount);
		final ClassWriter writer = new ClassWriter(reader, 0);
		final ClassVisitor visitor = new ClassProbesAdapter(
				new ClassInstrumenter(strategy, writer), true);
		reader.accept(visitor, ClassReader.EXPAND_FRAMES);

		final byte[] result = writer.toByteArray();
		if (java9) {
			Java9Support.upgrade(result);
		}
		return result;
	}

	private IProbeArrayStrategy createStrategyFor(final ClassLoader classLoader,
			final long classId, final String className, final int probeCount) {
		final Class<?> cls;
		final String companionName;
		final int fieldId;
		synchronized (companions) {
			Companion companion = companions.get(classLoader);
			if (companion == null) {
				companion = new Companion();
				companion.defineNew(classLoader);
				companions.put(classLoader, companion);
			} else if (companion.usedFields == FIELDS_PER_CLASS) {
				companion.defineNew(classLoader);
			}

			cls = companion.cls.get();
			// if class loader is alive, then class is also alive
			assert cls != null;
			companionName = companion.getName();
			fieldId = companion.usedFields;
			companion.usedFields++;
		}

		final boolean[] probes = runtimeData
				.getExecutionData(classId, className, probeCount).getProbes();
		try {
			// (Godin): There is possibility to get rid of usage of
			// reflection below - "clinit" can create an instance of a class
			// and store it in a static field, so that we can hold weak
			// reference on instance instead of class and can communicate
			// with it using "equals" method. However so far there is no
			// evidences that this optimization is required.
			cls.getMethod(INIT_METHOD_NAME, int.class, boolean[].class)
					.invoke(null, fieldId, probes);
		} catch (IllegalAccessException e) {
			throw new RuntimeException();
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}

		return new IProbeArrayStrategy() {
			public int storeInstance(MethodVisitor mv, int variable) {
				mv.visitFieldInsn(Opcodes.GETSTATIC, companionName,
						fieldNameFor(fieldId), InstrSupport.DATAFIELD_DESC);
				mv.visitVarInsn(Opcodes.ASTORE, variable);
				return 1;
			}

			public void addMembers(ClassVisitor cv, int probeCount) {
				// nothing to do
			}
		};
	}

	/**
	 * @return bytecode of generated "companion" class
	 */
	private static byte[] createCompanionClass(final String className) {
		final ClassWriter cw = new ClassWriter(0);

		cw.visit(Opcodes.V1_1,
				Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
				className, null, "java/lang/Object", null);

		cw.visitField(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_TRANSIENT,
				"nextSlot", Type.INT_TYPE.getDescriptor(), null, null);

		final Label[] labels = new Label[FIELDS_PER_CLASS];
		for (int fieldId = 0; fieldId < FIELDS_PER_CLASS; fieldId++) {
			labels[fieldId] = new Label();
			cw.visitField(
					Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC
							| Opcodes.ACC_TRANSIENT,
					fieldNameFor(fieldId), InstrSupport.DATAFIELD_DESC, null,
					null);
		}

		// void initialize(int field, boolean[] probes)
		MethodVisitor mv = cw.visitMethod(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, INIT_METHOD_NAME,
				"(I[Z)V", null, null);
		mv.visitCode();
		final Label start = new Label();
		final Label end = new Label();
		mv.visitLabel(start);
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitVarInsn(Opcodes.ILOAD, 0);
		mv.visitTableSwitchInsn(0, labels.length - 1, end, labels);
		for (int fieldId = 0; fieldId < labels.length; fieldId++) {
			mv.visitLabel(labels[fieldId]);
			mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
					fieldNameFor(fieldId), InstrSupport.DATAFIELD_DESC);
			mv.visitInsn(Opcodes.RETURN);
		}
		mv.visitLabel(end);
		mv.visitInsn(Opcodes.POP);
		mv.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalArgumentException");
		mv.visitInsn(Opcodes.DUP);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"java/lang/IllegalArgumentException", "<init>", "()V", false);
		mv.visitInsn(Opcodes.ATHROW);
		mv.visitEnd();
		mv.visitLocalVariable("fieldId", "I", null, start, end, 0);
		mv.visitLocalVariable("probes", InstrSupport.DATAFIELD_DESC, null,
				start, end, 1);
		mv.visitMaxs(2, 2);

		cw.visitEnd();

		return cw.toByteArray();
	}

	private static String fieldNameFor(int fieldId) {
		assert fieldId >= 0;
		return "p" + fieldId;
	}

}
