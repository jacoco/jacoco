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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Instrumentation with creation of "companion" class in "online" mode, which
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

	public static final String COMPANION_NAME = InstrSupport.DATAFIELD_NAME;

	/**
	 * https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html
	 */
	public static final int FIELDS_PER_CLASS = 65000;

	static class Companion {

		final WeakReference<Class<?>> cls;

		int usedFields;

		Companion(Class<?> cls) {
			// TODO(Godin):
			// we can utilize ReferenceQueue to cleanup unused probe arrays
			// https://github.com/jacoco/jacoco/issues/134
			this.cls = new WeakReference<Class<?>>(cls);
		}

	}

	private final Map<ClassLoader, Companion> companions = new WeakHashMap<ClassLoader, Companion>();

	private final RuntimeData runtimeData;

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
		final long classId = CRC64.checksum(buffer);
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
		final String field;
		synchronized (companions) {
			Companion companion = companions.get(classLoader);
			if (companion == null) {
				final byte[] definition = createCompanionClass();
				try {
					cls = (Class<?>) DEFINE_CLASS.invoke(classLoader,
							COMPANION_NAME, definition, 0, definition.length);
					companion = new Companion(cls);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				}
				companions.put(classLoader, companion);
			} else {
				cls = companion.cls.get();
				// if class loader is alive, then class is also alive
				assert cls != null;
			}

			if (companion.usedFields == FIELDS_PER_CLASS) {
				throw new UnsupportedOperationException();
			}
			field = "p" + companion.usedFields;
			companion.usedFields++;
		}

		final boolean[] probes = runtimeData
				.getExecutionData(classId, className, probeCount).getProbes();
		try {
			cls.getField(field).set(null, probes);
		} catch (IllegalAccessException e) {
			throw new RuntimeException();
		} catch (NoSuchFieldException e) {
			throw new RuntimeException();
		}

		return new IProbeArrayStrategy() {
			public int storeInstance(MethodVisitor mv, int variable) {
				mv.visitFieldInsn(Opcodes.GETSTATIC, COMPANION_NAME, field,
						InstrSupport.DATAFIELD_DESC);
				mv.visitVarInsn(Opcodes.ASTORE, variable);
				return 1;
			}

			public void addMembers(ClassVisitor cv, int probeCount) {
				// nothing to do
			}
		};
	}

	private byte[] createCompanionClass() {
		final ClassWriter cw = new ClassWriter(0);

		cw.visit(Opcodes.V1_1,
				Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
				COMPANION_NAME, null, "java/lang/Object", null);

		cw.visitField(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_TRANSIENT,
				"nextSlot", Type.INT_TYPE.getDescriptor(), null, null);

		for (int i = 0; i < FIELDS_PER_CLASS; i++) {
			cw.visitField(
					Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC
							| Opcodes.ACC_TRANSIENT,
					"p" + i, InstrSupport.DATAFIELD_DESC, null, null);
		}

		cw.visitEnd();
		return cw.toByteArray();
	}

}
