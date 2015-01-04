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
package org.jacoco.core.internal.instr;

import static java.lang.String.format;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Constants and utilities for byte code instrumentation.
 */
public final class InstrSupport {

	private InstrSupport() {
	}

	// === Data Field ===

	/**
	 * Name of the field that stores coverage information of a class.
	 */
	public static final String DATAFIELD_NAME = "$jacocoData";

	/**
	 * Access modifiers of the field that stores coverage information of a
	 * class.
	 */
	public static final int DATAFIELD_ACC = Opcodes.ACC_SYNTHETIC
			| Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_TRANSIENT
			| Opcodes.ACC_FINAL;

	/**
	 * Access modifiers of the field that stores coverage information of a Java
	 * 8 interface.
	 */
	public static final int DATAFIELD_INTF_ACC = Opcodes.ACC_SYNTHETIC
			| Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;

	/**
	 * Data type of the field that stores coverage information for a class (
	 * <code>boolean[]</code>).
	 */
	public static final String DATAFIELD_DESC = "[Z";

	// === Init Method ===

	/**
	 * Name of the initialization method.
	 */
	public static final String INITMETHOD_NAME = "$jacocoInit";

	/**
	 * Descriptor of the initialization method.
	 */
	public static final String INITMETHOD_DESC = "()[Z";

	/**
	 * Access modifiers of the initialization method.
	 */
	public static final int INITMETHOD_ACC = Opcodes.ACC_SYNTHETIC
			| Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC;

	/**
	 * Ensures that the given member does not correspond to a internal member
	 * created by the instrumentation process. This would mean that the class is
	 * already instrumented.
	 * 
	 * @param member
	 *            name of the member to check
	 * @param owner
	 *            name of the class owning the member
	 * @throws IllegalStateException
	 *             thrown if the member has the same name than the
	 *             instrumentation member
	 */
	public static void assertNotInstrumented(final String member,
			final String owner) throws IllegalStateException {
		if (member.equals(DATAFIELD_NAME) || member.equals(INITMETHOD_NAME)) {
			throw new IllegalStateException(format(
					"Class %s is already instrumented.", owner));
		}
	}

	/**
	 * Generates the instruction to push the given int value on the stack.
	 * Implementation taken from
	 * {@link org.objectweb.asm.commons.GeneratorAdapter#push(int)}.
	 * 
	 * @param mv
	 *            visitor to emit the instruction
	 * @param value
	 *            the value to be pushed on the stack.
	 */
	public static void push(final MethodVisitor mv, final int value) {
		if (value >= -1 && value <= 5) {
			mv.visitInsn(Opcodes.ICONST_0 + value);
		} else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			mv.visitIntInsn(Opcodes.BIPUSH, value);
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			mv.visitIntInsn(Opcodes.SIPUSH, value);
		} else {
			mv.visitLdcInsn(Integer.valueOf(value));
		}
	}

}
