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
	void accept(final MethodVisitor mv);

}
