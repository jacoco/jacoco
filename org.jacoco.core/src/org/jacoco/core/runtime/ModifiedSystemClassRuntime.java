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

import static java.lang.String.format;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.internal.Java9Support;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This {@link IRuntime} implementation works with a modified system class. A
 * new static method is added to a bootstrap class that will be used by
 * instrumented classes. As the system class itself needs to be instrumented
 * this runtime requires a Java agent.
 */
public class ModifiedSystemClassRuntime extends AbstractRuntime {

	private static final String ACCESS_FIELD_TYPE = "Ljava/lang/Object;";

	private final Class<?> systemClass;

	private final String systemClassName;

	private final String accessFieldName;

	/**
	 * Creates a new runtime based on the given class and members.
	 * 
	 * @param systemClass
	 *            system class that contains the execution data
	 * @param accessFieldName
	 *            name of the public static runtime access field
	 * 
	 */
	public ModifiedSystemClassRuntime(final Class<?> systemClass,
			final String accessFieldName) {
		super();
		this.systemClass = systemClass;
		this.systemClassName = systemClass.getName().replace('.', '/');
		this.accessFieldName = accessFieldName;
	}

	@Override
	public void startup(final RuntimeData data) throws Exception {
		super.startup(data);
		final Field field = systemClass.getField(accessFieldName);
		field.set(null, data);
	}

	public void shutdown() {
		// nothing to do
	}

	public int generateDataAccessor(final long classid, final String classname,
			final int probecount, final MethodVisitor mv) {

		mv.visitFieldInsn(Opcodes.GETSTATIC, systemClassName, accessFieldName,
				ACCESS_FIELD_TYPE);

		RuntimeData.generateAccessCall(classid, classname, probecount, mv);

		return 6;
	}

	/**
	 * Creates a new {@link ModifiedSystemClassRuntime} using the given class as
	 * the data container. Members are creates with internal default names. The
	 * given class must not have been loaded before by the agent.
	 * 
	 * @param inst
	 *            instrumentation interface
	 * @param className
	 *            VM name of the class to use
	 * @return new runtime instance
	 * 
	 * @throws ClassNotFoundException
	 *             id the given class can not be found
	 */
	public static IRuntime createFor(final Instrumentation inst,
			final String className) throws ClassNotFoundException {
		return createFor(inst, className, "$jacocoAccess");
	}

	/**
	 * Creates a new {@link ModifiedSystemClassRuntime} using the given class as
	 * the data container. The given class must not have been loaded before by
	 * the agent.
	 * 
	 * @param inst
	 *            instrumentation interface
	 * @param className
	 *            VM name of the class to use
	 * @param accessFieldName
	 *            name of the added runtime access field
	 * @return new runtime instance
	 * 
	 * @throws ClassNotFoundException
	 *             id the given class can not be found
	 */
	public static IRuntime createFor(final Instrumentation inst,
			final String className, final String accessFieldName)
			throws ClassNotFoundException {
		final ClassFileTransformer transformer = new ClassFileTransformer() {
			public byte[] transform(final ClassLoader loader,
					final String name, final Class<?> classBeingRedefined,
					final ProtectionDomain protectionDomain, final byte[] source)
					throws IllegalClassFormatException {
				if (name.equals(className)) {
					return instrument(source, accessFieldName);
				}
				return null;
			}
		};
		inst.addTransformer(transformer);
		final Class<?> clazz = Class.forName(className.replace('/', '.'));
		inst.removeTransformer(transformer);
		try {
			clazz.getField(accessFieldName);
		} catch (final NoSuchFieldException e) {
			throw new RuntimeException(format(
					"Class %s could not be instrumented.", className), e);
		}
		return new ModifiedSystemClassRuntime(clazz, accessFieldName);
	}

	/**
	 * Adds the static access method and data field to the given class
	 * definition.
	 * 
	 * @param source
	 *            class definition source
	 * @param accessFieldName
	 *            name of the runtime access field
	 * @return instrumented version with added members
	 */
	public static byte[] instrument(final byte[] source,
			final String accessFieldName) {
		final ClassReader reader = new ClassReader(Java9Support.downgradeIfRequired(source));
		final ClassWriter writer = new ClassWriter(reader, 0);
		reader.accept(new ClassVisitor(JaCoCo.ASM_API_VERSION, writer) {

			@Override
			public void visitEnd() {
				createDataField(cv, accessFieldName);
				super.visitEnd();
			}

		}, ClassReader.EXPAND_FRAMES);
		return writer.toByteArray();
	}

	private static void createDataField(final ClassVisitor visitor,
			final String dataField) {
		visitor.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC
				| Opcodes.ACC_SYNTHETIC | Opcodes.ACC_TRANSIENT, dataField,
				ACCESS_FIELD_TYPE, null, null);
	}

}
