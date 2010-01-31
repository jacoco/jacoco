/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and others
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

import static java.lang.String.format;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassAdapter;
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
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ModifiedSystemClassRuntime extends AbstractRuntime {

	private final Class<?> systemClass;

	private final String systemClassName;

	private final String accessMethod;

	private final String dataField;

	/**
	 * Creates a new runtime based on the given class and members.
	 * 
	 * @param systemClass
	 *            system class that contains the execution data
	 * @param accessMethod
	 *            name of the public static access method
	 * @param dataField
	 *            name of the public static data field
	 * 
	 */
	public ModifiedSystemClassRuntime(final Class<?> systemClass,
			final String accessMethod, final String dataField) {
		this.systemClass = systemClass;
		this.systemClassName = systemClass.getName().replace('.', '/');
		this.accessMethod = accessMethod;
		this.dataField = dataField;
	}

	public void startup() throws Exception {
		final Field field = systemClass.getField(dataField);
		field.set(null, new MapAdapter(store));
	}

	public void shutdown() {
		// nothing to do
	}

	public int generateDataAccessor(final long classid, final MethodVisitor mv) {

		mv.visitLdcInsn(Long.valueOf(classid));

		// Stack[1]: J
		// Stack[0]: .

		mv.visitMethodInsn(Opcodes.INVOKESTATIC, systemClassName, accessMethod,
				"(J)[Z");

		// Stack[0]: [Z

		return 2;
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
		return createFor(inst, className, "$jacocoGet", "$jacocoData");
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
	 * @param accessMethod
	 *            name of the added access method
	 * @param dataField
	 *            name of the added data field
	 * @return new runtime instance
	 * 
	 * @throws ClassNotFoundException
	 *             id the given class can not be found
	 */
	public static IRuntime createFor(final Instrumentation inst,
			final String className, final String accessMethod,
			final String dataField) throws ClassNotFoundException {
		final boolean[] instrumented = new boolean[] { false };
		final ClassFileTransformer transformer = new ClassFileTransformer() {
			public byte[] transform(final ClassLoader loader,
					final String name, final Class<?> classBeingRedefined,
					final ProtectionDomain protectionDomain, final byte[] source)
					throws IllegalClassFormatException {
				if (name.equals(className)) {
					instrumented[0] = true;
					return instrument(source, accessMethod, dataField);
				}
				return null;
			}
		};
		inst.addTransformer(transformer);
		final Class<?> clazz = Class.forName(className.replace('/', '.'));
		inst.removeTransformer(transformer);
		if (!instrumented[0]) {
			final String msg = format("Class %s was not loaded.", className);
			throw new RuntimeException(msg);
		}
		return new ModifiedSystemClassRuntime(clazz, accessMethod, dataField);
	}

	/**
	 * Adds the static access method and data field to the given class
	 * definition.
	 * 
	 * @param source
	 *            class definition source
	 * @param accessMethod
	 *            name of the access method
	 * @param dataField
	 *            name of the data field
	 * @return instrumented version with added members
	 */
	public static byte[] instrument(final byte[] source,
			final String accessMethod, final String dataField) {
		final ClassReader reader = new ClassReader(source);
		final ClassWriter writer = new ClassWriter(reader, 0);
		reader.accept(new ClassAdapter(writer) {

			private String className;

			@Override
			public void visit(final int version, final int access,
					final String name, final String signature,
					final String superName, final String[] interfaces) {
				className = name;
				super.visit(version, access, name, signature, superName,
						interfaces);
			}

			@Override
			public void visitEnd() {
				createDataField(cv, dataField);
				createAccessMethod(cv, className, accessMethod, dataField);
				super.visitEnd();
			}

		}, ClassReader.EXPAND_FRAMES);
		return writer.toByteArray();
	}

	private static void createDataField(final ClassVisitor visitor,
			final String dataField) {
		visitor.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, dataField,
				"Ljava/util/Map;", "Ljava/util/Map<Ljava/lang/Long;[Z>;", null);
	}

	private static void createAccessMethod(final ClassVisitor visitor,
			final String className, final String accessMethod,
			final String dataField) {
		final int access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
		final String desc = "(J)[Z";
		final MethodVisitor mv = visitor.visitMethod(access, accessMethod,
				desc, null, null);

		mv.visitFieldInsn(Opcodes.GETSTATIC, className, dataField,
				"Ljava/util/Map;");

		// Stack[0]: Ljava/util/Map;

		mv.visitVarInsn(Opcodes.LLOAD, 0);

		// Stack[2]: J
		// Stack[1]: .
		// Stack[0]: Ljava/util/Map;

		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf",
				"(J)Ljava/lang/Long;");

		// Stack[1]: Ljava/lang/Long;
		// Stack[0]: Ljava/util/Map;

		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "get",
				"(Ljava/lang/Object;)Ljava/lang/Object;");

		// Stack[0]: Ljava/lang/Object;

		mv.visitTypeInsn(Opcodes.CHECKCAST, "[Z");

		// Stack[0]: [Z

		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(3, 2);
		mv.visitEnd();
	}

}
