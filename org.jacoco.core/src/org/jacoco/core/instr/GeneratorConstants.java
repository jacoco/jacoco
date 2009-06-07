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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

/**
 * Constants for generated instrumentation code.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public final class GeneratorConstants {

	/**
	 * Type for array of primitive boolean values. This type is used to store
	 * covered blocks of a single method.
	 */
	public static final Type BLOCK_ARR = Type.getType("[Z");

	// === Data Field ===

	/**
	 * Name of the field that stores coverage information of a class.
	 */
	public static final String DATAFIELD_NAME = "$jacocoData";

	/**
	 * Access modifiers of the field that stores coverage information of a
	 * class.
	 */
	public static final int DATAFIELD_ACC = Opcodes.ACC_SYNTHETIC
			| Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;

	/**
	 * The type of the field that stores coverage information of a class is a
	 * 2-dimensional array of primitive boolean values.
	 */
	public static final Type DATAFIELD_TYPE = Type.getType("[[Z");

	// === Init Method ===

	/**
	 * Initialization method that is added into every instrumented class.
	 */
	public static final Method INIT_METHOD = new Method("$jacocoInit", "(I)[Z");

	/**
	 * Access modifiers of the initialization method.
	 */
	public static final int INIT_METHOD_ACC = Opcodes.ACC_SYNTHETIC
			| Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;

}
