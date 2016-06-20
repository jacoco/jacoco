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
package org.jacoco.core.runtime;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.internal.instr.Companions;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Strategy that creates "companion" classes in "offline" mode, which will hold
 * state of instrumented classes. This allows to avoid addition of members to
 * classes.
 */
public class OfflineInstrumentationCompanionAccessGenerator
		implements IExecutionDataAccessorGenerator {

	private final String runtimeClassName;

	private String companionName;

	private final Set<String> instrumented = new HashSet<String>();

	private ClassWriter cw;

	private MethodVisitor mv;

	/**
	 * Creates a new instance for offline instrumentation.
	 */
	public OfflineInstrumentationCompanionAccessGenerator() {
		this(JaCoCo.RUNTIMEPACKAGE.replace('.', '/') + "/Offline");
	}

	OfflineInstrumentationCompanionAccessGenerator(
			final String runtimeClassName) {
		this.runtimeClassName = runtimeClassName;
		newCompanion();
	}

	public int generateDataAccessor(final long classid, final String classname,
			final int probecount, final MethodVisitor mv) {
		if (instrumented.size() == Companions.FIELDS_PER_CLASS) {
			throw new UnsupportedOperationException();
		}
		final String fieldName = fieldNameFor(classid);
		if (instrumented.add(classname)) {
			cw.visitField(
					Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC
							| Opcodes.ACC_TRANSIENT | Opcodes.ACC_FINAL,
					fieldName, InstrSupport.DATAFIELD_DESC, null, null);
			generateInitializer(fieldName, classid, classname, probecount);
		}
		mv.visitFieldInsn(Opcodes.GETSTATIC, companionName, fieldName,
				InstrSupport.DATAFIELD_DESC);
		return 1;
	}

	/**
	 * Returns the number of instrumented classes since last invocation of
	 * {@link #getClassDefinition()}.
	 * 
	 * @return number of instrumented classes since last invocation of
	 *         {@link #getClassDefinition()}
	 */
	public int getNumberOfInstrumentedClasses() {
		return instrumented.size();
	}

	/**
	 * Returns the name of generated "companion" class.
	 *
	 * @return name of generated "companion" class
	 */
	public String getClassName() {
		return companionName;
	}

	private void newCompanion() {
		instrumented.clear();
		companionName = Companions.COMPANION_NAME
				+ UUID.randomUUID().toString().replace('-', '_');

		cw = new ClassWriter(0);
		cw.visit(Opcodes.V1_1,
				Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
				companionName, null, "java/lang/Object", null);
		mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
		mv.visitCode();
	}

	private void generateInitializer(final String fieldName, final long
			classId, final String className, final int probeCount) {
		mv.visitLdcInsn(Long.valueOf(classId));
		mv.visitLdcInsn(className);
		InstrSupport.push(this.mv, probeCount);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeClassName,
				"getProbes", "(JLjava/lang/String;I)[Z", false);
		mv.visitFieldInsn(Opcodes.PUTSTATIC, companionName, fieldName,
				InstrSupport.DATAFIELD_DESC);
		// Maximum local stack size is 4
	}

	/**
	 * Returns the bytecode of generated "companion" class.
	 *
	 * @return bytecode of generated "companion" class
	 */
	public byte[] getClassDefinition() {
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(4, 0);
		mv.visitEnd();
		cw.visitEnd();
		final byte[] result = cw.toByteArray();
		newCompanion();
		return result;
	}

	/**
	 * https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.2.2
	 */
	static String fieldNameFor(long classId) {
		return "p" + Long.toHexString(classId);
	}

}
