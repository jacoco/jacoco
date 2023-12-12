/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Interface used by filters to mark filtered items.
 */
public interface IFilterOutput {

	/**
	 * Marks sequence of instructions that should be ignored during computation
	 * of coverage.
	 *
	 * @param fromInclusive
	 *            first instruction that should be ignored, inclusive
	 * @param toInclusive
	 *            last instruction coming after <code>fromInclusive</code> that
	 *            should be ignored, inclusive
	 */
	void ignore(AbstractInsnNode fromInclusive, AbstractInsnNode toInclusive);

	/**
	 * Marks two instructions that should be merged during computation of
	 * coverage.
	 *
	 * @param i1
	 *            first instruction
	 * @param i2
	 *            second instruction
	 */
	void merge(AbstractInsnNode i1, AbstractInsnNode i2);

	/**
	 * Marks instruction whose outgoing branches should be replaced during
	 * computation of coverage.
	 *
	 * @param source
	 *            instruction which branches should be replaced
	 * @param newTargets
	 *            new targets of branches
	 */
	void replaceBranches(AbstractInsnNode source,
			Set<AbstractInsnNode> newTargets);

}
