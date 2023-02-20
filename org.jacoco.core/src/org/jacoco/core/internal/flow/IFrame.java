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
package org.jacoco.core.internal.flow;

import org.objectweb.asm.MethodVisitor;

/**
 * Representation of the current stackmap frame content.
 */
public interface IFrame {

	/**
	 * Emits a frame event with the current content to the given visitor.
	 *
	 * @param mv
	 *            method visitor to emit frame event to
	 */
	void accept(MethodVisitor mv);

}
