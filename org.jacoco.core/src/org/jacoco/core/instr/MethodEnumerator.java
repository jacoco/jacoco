/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * A {@link ClassVisitor} that gives each non-abstract method a unique id that
 * corresponds to its index in the class file.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */

public abstract class MethodEnumerator implements ClassVisitor {

	private int counter = 0;

	public final MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {
		if ((access & Opcodes.ACC_ABSTRACT) == 0) {
			return visitMethod(counter++, access, name, desc, signature,
					exceptions);
		} else {
			return visitAbstractMethod(access, name, desc, signature,
					exceptions);
		}
	}

	/**
	 * This method is called for every non-abstract method.
	 * 
	 * @param methodId
	 *            unique id for this method
	 * @param access
	 *            the method's access flags (see {@link Opcodes}). This
	 *            parameter also indicates if the method is synthetic and/or
	 *            deprecated.
	 * @param name
	 *            the method's name.
	 * @param desc
	 *            the method's descriptor (see {@link Type Type}).
	 * @param signature
	 *            the method's signature. May be <tt>null</tt> if the method
	 *            parameters, return type and exceptions do not use generic
	 *            types.
	 * @param exceptions
	 *            the internal names of the method's exception classes (see
	 *            {@link Type#getInternalName() getInternalName}). May be
	 *            <tt>null</tt>.
	 * @return an object to visit the byte code of the method, or <tt>null</tt>
	 *         if this class visitor is not interested in visiting the code of
	 *         this method.
	 */
	protected abstract MethodVisitor visitMethod(final int methodId,
			final int access, final String name, final String desc,
			final String signature, final String[] exceptions);

	/**
	 * This method is called for every abstract method.
	 * 
	 * @param access
	 *            the method's access flags (see {@link Opcodes}). This
	 *            parameter also indicates if the method is synthetic and/or
	 *            deprecated.
	 * @param name
	 *            the method's name.
	 * @param desc
	 *            the method's descriptor (see {@link Type Type}).
	 * @param signature
	 *            the method's signature. May be <tt>null</tt> if the method
	 *            parameters, return type and exceptions do not use generic
	 *            types.
	 * @param exceptions
	 *            the internal names of the method's exception classes (see
	 *            {@link Type#getInternalName() getInternalName}). May be
	 *            <tt>null</tt>.
	 * @return an object to visit the byte code of the method, or <tt>null</tt>
	 *         if this class visitor is not interested in visiting the code of
	 *         this method.
	 */
	protected abstract MethodVisitor visitAbstractMethod(final int access,
			final String name, final String desc, final String signature,
			final String[] exceptions);

	/**
	 * Returns the total number of non abstract methods of the processed class.
	 * 
	 * @return number of non-abstract methods
	 */
	protected final int getMethodCount() {
		return counter;
	}

}
