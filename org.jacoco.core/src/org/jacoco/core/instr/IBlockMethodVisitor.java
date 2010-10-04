/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.instr;

import org.objectweb.asm.MethodVisitor;

/**
 * A method visitor with additional notifications for block boundaries.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
interface IBlockMethodVisitor extends MethodVisitor {

	/**
	 * This method is called at the end of a block. If the last instruction of
	 * the block may interrupt the control flow (e.g. jump or return) this
	 * method is called right before this statement is visited.
	 * 
	 * @param id
	 *            identifier of the block within the method
	 */
	public void visitBlockEndBeforeJump(int id);

	/**
	 * This method is always called after the last instruction of a block.
	 * 
	 * @param id
	 *            identifier of the block within the method
	 */
	public void visitBlockEnd(int id);

}
