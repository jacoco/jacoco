/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.pattern;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * A pattern is a immutable definition of some structure that can be matches
 * against a bytecode sequence. It is stateless and can be (re-)used
 * concurrently.
 */
public interface IPattern {

	/**
	 * Matches from the beginning of the pattern starting from a given node.
	 * 
	 * @param startNode
	 *            node to start matching with
	 * @return last matched node or <code>null</code> if pattern does not match
	 */
	AbstractInsnNode matchForward(AbstractInsnNode startNode);

}
