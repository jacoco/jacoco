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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * A {@link ClassVisitor} with additional methods to get probe insertion
 * information for each method
 */
public abstract class ClassProbesVisitor extends ClassVisitor {

	/**
	 * New visitor instance without delegate visitor.
	 */
	public ClassProbesVisitor() {
		this(null);
	}

	/**
	 * New visitor instance that delegates to the given visitor.
	 * 
	 * @param cv
	 *            optional next visitor in chain
	 */
	public ClassProbesVisitor(final ClassVisitor cv) {
		super(Opcodes.ASM4, cv);
	}

	/**
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
	 * @return an object to visit the byte code of the method <b>before</b>
	 *         {@link #visitMethod} is called, or <tt>null</tt> if this class
	 *         visitor is not interested in pre-visiting the code of this
	 *         method.
	 */
	public abstract MethodVisitor preVisitMethod(int access, String name,
			String desc, String signature, String[] exceptions);

	/**
	 * When visiting a method we need a {@link MethodProbesVisitor} to handle
	 * the probes of that method.
	 */
	@Override
	public abstract MethodProbesVisitor visitMethod(int access, String name,
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
	public abstract void visitTotalProbeCount(int count);

}
