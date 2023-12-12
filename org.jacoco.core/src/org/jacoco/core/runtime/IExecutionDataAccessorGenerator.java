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

import org.objectweb.asm.MethodVisitor;

/**
 * The instrumented classes need a piece of code that obtains a
 * <code>boolean[]</code> instance from the runtime. The mechanism is runtime
 * specific and therefore abstracted by this interface. Implementations are
 * provided by {@link IRuntime} implementations and are used by the
 * instrumentation process.
 */
public interface IExecutionDataAccessorGenerator {

	/**
	 * This method generates the byte code required to obtain the coverage data
	 * structure for the class with the given id. Typically the instrumentation
	 * process will embed this code into a method that is called on class
	 * initialization. This method can be called at any time even outside the
	 * target VM.
	 *
	 * The generated code must push a <code>boolean[]</code> instance to the
	 * operand stack. Except this result object the generated code must not make
	 * any assumptions about the structure of the embedding method or class. The
	 * generated code must not use or allocate local variables.
	 *
	 * @param classid
	 *            identifier of the class
	 * @param classname
	 *            VM class name
	 * @param probecount
	 *            probe count for this class
	 * @param mv
	 *            code output
	 * @return additional stack size required by the implementation, including
	 *         the instance pushed to the stack
	 */
	int generateDataAccessor(long classid, String classname, int probecount,
			MethodVisitor mv);

}
