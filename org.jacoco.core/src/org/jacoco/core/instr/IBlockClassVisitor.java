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
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.instr;

import org.objectweb.asm.ClassVisitor;

/**
 * A class visitor with additional notifications for block boundaries for all
 * methods.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
interface IBlockClassVisitor extends ClassVisitor {

	public IBlockMethodVisitor visitMethod(int access, String name,
			String desc, String signature, String[] exceptions);

	/**
	 * Reports the total number of encountered probes. This method is called
	 * just before {@link ClassVisitor#visitEnd()}.
	 * 
	 * @param count
	 *            total number of probes
	 */
	public void visitTotalProbeCount(int count);

}
