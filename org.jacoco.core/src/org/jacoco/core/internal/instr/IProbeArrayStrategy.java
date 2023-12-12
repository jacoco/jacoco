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
package org.jacoco.core.internal.instr;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Strategies to retrieve the probe array instance for each method within a
 * type. This abstraction is required as we need to follow a different strategy
 * depending on whether the instrumented type is a class or interface.
 */
public interface IProbeArrayStrategy {

	/**
	 * Creates code that stores the probe array instance in the given variable.
	 *
	 * @param mv
	 *            visitor to create code
	 * @param clinit
	 *            true in case of {@code <clinit>} method
	 * @param variable
	 *            variable index to store probe array to
	 * @return maximum stack size required by the generated code
	 */
	int storeInstance(MethodVisitor mv, boolean clinit, int variable);

	/**
	 * Adds additional class members required by this strategy. This method is
	 * called after all original members of the class has been processed.
	 *
	 * @param cv
	 *            visitor to create fields and methods
	 * @param probeCount
	 *            total number of probes required for this class
	 */
	void addMembers(ClassVisitor cv, int probeCount);

}
