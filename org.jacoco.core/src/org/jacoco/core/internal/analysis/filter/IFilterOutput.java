/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

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

}
