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
package org.jacoco.core.runtime;

import org.jacoco.core.data.IExecutionDataVisitor;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * This interface represents a particular mechanism to collect execution
 * information in the target VM at runtime.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface IRuntime {

	/**
	 * This method generates the byte code required to register the coverage
	 * data structure of the class with the given id. Typically the
	 * instrumentation process will embed this code into a method that is called
	 * on class initialization. This method can be called at any time even
	 * outside the target VM.
	 * 
	 * The generated code must pop a <code>byte[][]</code> instance from the
	 * operand stack. Except this object on the stack the generated code must
	 * not make any assumptions about the structure of the embedding method or
	 * class.
	 * 
	 * @param classId
	 *            identifier of the class
	 * @param gen
	 *            code output
	 */
	public void generateRegistration(long classId, GeneratorAdapter gen);

	/**
	 * Starts the coverage runtime. This method MUST be called before any class
	 * instrumented for this runtime is loaded.
	 */
	public void startup();

	/**
	 * Allows the coverage runtime to cleanup internals. This class should be
	 * called when classes instrumented for this runtime are not used any more.
	 */
	public void shutdown();

	/**
	 * Collects the current execution data and writes it to the given
	 * {@link IExecutionDataVisitor} object. This method must only be called
	 * between {@link #startup()} and {@link #shutdown()}.
	 * 
	 * @param visitor
	 *            handler to write coverage data to
	 * @param reset
	 *            if <code>true</code> the current coverage information is also
	 *            cleared
	 */
	public void collect(IExecutionDataVisitor visitor, boolean reset);

	/**
	 * Resets all coverage information. This method must only be called between
	 * {@link #startup()} and {@link #shutdown()}.
	 */
	public void reset();

}
