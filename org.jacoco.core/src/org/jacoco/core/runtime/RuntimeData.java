/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.runtime;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Container for runtime execution and meta data. All access to the runtime data
 * is thread safe.
 */
public class RuntimeData {

	/** store for execution data */
	protected final ExecutionDataStore store;

	private long startTimeStamp;

	private String sessionId;

	/**
	 * Creates a new runtime.
	 */
	public RuntimeData() {
		store = new ExecutionDataStore();
		sessionId = "<none>";
		startTimeStamp = System.currentTimeMillis();
	}

	/**
	 * Sets a session identifier for this runtime. The identifier is used when
	 * execution data is collected. If no identifier is explicitly set a
	 * identifier is generated from the host name and a random number. This
	 * method can be called at any time.
	 *
	 * @see #collect(IExecutionDataVisitor, ISessionInfoVisitor, boolean)
	 * @param id
	 *            new session identifier
	 */
	public void setSessionId(final String id) {
		sessionId = id;
	}

	/**
	 * Get the current a session identifier for this runtime.
	 *
	 * @see #setSessionId(String)
	 * @return current session identifier
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Collects the current execution data and writes it to the given
	 * {@link IExecutionDataVisitor} object.
	 *
	 * @param executionDataVisitor
	 *            handler to write coverage data to
	 * @param sessionInfoVisitor
	 *            handler to write session information to
	 * @param reset
	 *            if <code>true</code> the current coverage information is also
	 *            cleared
	 */
	public final void collect(final IExecutionDataVisitor executionDataVisitor,
			final ISessionInfoVisitor sessionInfoVisitor, final boolean reset) {
		synchronized (store) {
			final SessionInfo info = new SessionInfo(sessionId, startTimeStamp,
					System.currentTimeMillis());
			sessionInfoVisitor.visitSessionInfo(info);
			store.accept(executionDataVisitor);
			if (reset) {
				reset();
			}
		}
	}

	/**
	 * Resets all coverage information.
	 */
	public final void reset() {
		synchronized (store) {
			store.reset();
			startTimeStamp = System.currentTimeMillis();
		}
	}

	/**
	 * Returns the coverage data for the class with the given identifier. If
	 * there is no data available under the given id a new entry is created.
	 * This is a synchronized access to the underlying store.
	 *
	 * @param id
	 *            class identifier
	 * @param name
	 *            VM name of the class
	 * @param probecount
	 *            probe data length
	 * @return execution data
	 */
	public ExecutionData getExecutionData(final Long id, final String name,
			final int probecount) {
		synchronized (store) {
			return store.get(id, name, probecount);
		}
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
	public void getProbes(final Object[] args) {
		final Long classid = (Long) args[0];
		final String name = (String) args[1];
		final int probecount = ((Integer) args[2]).intValue();
		args[0] = getExecutionData(classid, name, probecount).getProbes();
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
		if (args instanceof Object[]) {
			getProbes((Object[]) args);
		}
		return super.equals(args);
	}

	/**
	 * Generates code that creates the argument array for the
	 * {@link #getProbes(Object[])} method. The array instance is left on the
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
			final String classname, final int probecount,
			final MethodVisitor mv) {
		mv.visitInsn(Opcodes.ICONST_3);
		mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");

		// Class Id:
		mv.visitInsn(Opcodes.DUP);
		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitLdcInsn(Long.valueOf(classid));
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf",
				"(J)Ljava/lang/Long;", false);
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
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf",
				"(I)Ljava/lang/Integer;", false);
		mv.visitInsn(Opcodes.AASTORE);
	}

	/**
	 * Generates the code that calls a {@link RuntimeData} instance through the
	 * JRE API method {@link Object#equals(Object)}. The code pops a
	 * {@link Object} instance from the stack and pushes the probe array of type
	 * <code>boolean[]</code> on the operand stack. The generated code requires
	 * a stack size of 6.
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
	public static void generateAccessCall(final long classid,
			final String classname, final int probecount,
			final MethodVisitor mv) {
		// stack[0]: Ljava/lang/Object;

		generateArgumentArray(classid, classname, probecount, mv);

		// stack[1]: [Ljava/lang/Object;
		// stack[0]: Ljava/lang/Object;

		mv.visitInsn(Opcodes.DUP_X1);

		// stack[2]: [Ljava/lang/Object;
		// stack[1]: Ljava/lang/Object;
		// stack[0]: [Ljava/lang/Object;

		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "equals",
				"(Ljava/lang/Object;)Z", false);
		mv.visitInsn(Opcodes.POP);

		// stack[0]: [Ljava/lang/Object;

		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitInsn(Opcodes.AALOAD);

		// stack[0]: [Z

		mv.visitTypeInsn(Opcodes.CHECKCAST, InstrSupport.DATAFIELD_DESC);
	}

}
