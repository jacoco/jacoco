/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This class implements the access from instrumented classes to execution data
 * storage in the runtime. Instead of directly referencing JaCoCo implementation
 * classes the access is decoupled through a JRE API. This avoids dependencies
 * from instrumented classes on JaCoCo APIs.
 * 
 * The JRE interface method used is {@link Object#equals(Object)} where the
 * passed argument is an {@link Object} array containing the class id (
 * {@link Long}), the class vm name ({@link String}) and the probe count (
 * {@link Integer}). After the method call the probe array instance is stored in
 * the first slot of the {@link Object} array.
 */
class ExecutionDataAccess {

	private final ExecutionDataStore store;

	ExecutionDataAccess(final ExecutionDataStore store) {
		this.store = store;
	}

	/**
	 * Retrieves the execution probe array for a given class. The passed
	 * {@link Object} array instance is used for parameters and the return value
	 * as follows. Call parameters:
	 * 
	 * <ul>
	 * <li>args[0]: class id ({@link Long})
	 * <li>args[1]: vm class name ({@link String})
	 * <li>args[2]: probe count ({@link Integer})
	 * </ul>
	 * 
	 * Return value:
	 * 
	 * <ul>
	 * <li>args[0]: probe array (<code>boolean[]</code>)
	 * </ul>
	 * 
	 * @param args
	 *            parameter array of length 3
	 */
	public void getExecutionData(final Object[] args) {
		final Long classid = (Long) args[0];
		final String name = (String) args[1];
		final int probecount = ((Integer) args[2]).intValue();
		synchronized (store) {
			args[0] = store.get(classid, name, probecount).getData();
		}
	}

	/**
	 * Generates code that creates the argument array for the
	 * <code>getExecutionData()</code> method. The array instance is left on the
	 * operand stack. The generated code requires a stack size of 5.
	 * 
	 * @param classid
	 *            class identifier
	 * @param classname
	 *            VM class name
	 * @param probecount
	 *            probe count for this class
	 * @param mv
	 *            visitor to emit generated code
	 */
	public static void generateArgumentArray(final long classid,
			final String classname, final int probecount, final MethodVisitor mv) {
		mv.visitInsn(Opcodes.ICONST_3);
		mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");

		// Class Id:
		mv.visitInsn(Opcodes.DUP);
		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitLdcInsn(Long.valueOf(classid));
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf",
				"(J)Ljava/lang/Long;");
		mv.visitInsn(Opcodes.AASTORE);

		// Class Name:
		mv.visitInsn(Opcodes.DUP);
		mv.visitInsn(Opcodes.ICONST_1);
		mv.visitLdcInsn(classname);
		mv.visitInsn(Opcodes.AASTORE);

		// Probe Count:
		mv.visitInsn(Opcodes.DUP);
		mv.visitInsn(Opcodes.ICONST_2);
		InstrSupport.push(mv, probecount);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer",
				"valueOf", "(I)Ljava/lang/Integer;");
		mv.visitInsn(Opcodes.AASTORE);
	}

	/**
	 * Generates the code that calls the runtime data access through the JRE API
	 * method {@link Object#equals(Object)}. The code pops a {@link Object}
	 * instance from the stack and pushes the probe array of type
	 * <code>boolean[]</code> on the operand stack. The generated code requires
	 * a stack size of 6.
	 * 
	 * @param classid
	 * @param classname
	 * @param probecount
	 * @param mv
	 */
	public static void generateAccessCall(final long classid,
			final String classname, final int probecount, final MethodVisitor mv) {
		// stack[0]: Ljava/lang/Object;

		generateArgumentArray(classid, classname, probecount, mv);

		// stack[1]: [Ljava/lang/Object;
		// stack[0]: Ljava/lang/Object;

		mv.visitInsn(Opcodes.DUP_X1);

		// stack[2]: [Ljava/lang/Object;
		// stack[1]: Ljava/lang/Object;
		// stack[0]: [Ljava/lang/Object;

		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "equals",
				"(Ljava/lang/Object;)Z");
		mv.visitInsn(Opcodes.POP);

		// stack[0]: [Ljava/lang/Object;

		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitInsn(Opcodes.AALOAD);

		// stack[0]: [Z

		mv.visitTypeInsn(Opcodes.CHECKCAST, InstrSupport.DATAFIELD_DESC);
	}

	/**
	 * In violation of the regular semantic of {@link Object#equals(Object)}
	 * this implementation is used as the interface to the execution data store.
	 * 
	 * @param args
	 *            the arguments as an {@link Object} array
	 * @return has no meaning
	 */
	@Override
	public boolean equals(final Object args) {
		getExecutionData((Object[]) args);
		return false;
	}

}
