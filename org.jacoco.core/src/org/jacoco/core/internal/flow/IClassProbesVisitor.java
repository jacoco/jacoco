/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
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

import org.objectweb.asm.ClassVisitor;

/**
 * A {@link ClassVisitor} with additional methods to get probe insertion
 * information for each method
 */
public interface IClassProbesVisitor extends ClassVisitor {

	public IMethodProbesVisitor visitMethod(int access, String name,
			String desc, String signature, String[] exceptions);

	/**
	 * Reports the total number of encountered probes. For classes this method
	 * is called just before {@link ClassVisitor#visitEnd()}. For interfaces
	 * this method is called before the first method (the static initializer) is
	 * emitted.
	 * 
	 * @param count
	 *            total number of probes
	 */
	public void visitTotalProbeCount(int count);

}
