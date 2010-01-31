/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and others
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
import org.objectweb.asm.MethodVisitor;

/**
 * This interface represents a particular mechanism to collect execution
 * information in the target VM at runtime.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface IRuntime {

	/**
	 * This method generates the byte code required to obtain the coverage data
	 * structure for the class with the given id. Typically the instrumentation
	 * process will embed this code into a method that is called on class
	 * initialization. This method can be called at any time even outside the
	 * target VM.
	 * 
	 * The generated code must push a <code>boolean[]</code> instance to the
	 * operand stack. Except this result object the generated code must not make
	 * any assumptions about the structure of the embedding method or class.
	 * 
	 * @param classid
	 *            identifier of the class
	 * @param mv
	 *            code output
	 * @return additional stack size required by the implementation, including
	 *         the instance pushed to the stack
	 */
	public int generateDataAccessor(long classid, MethodVisitor mv);

	/**
	 * Starts the coverage runtime. This method MUST be called before any class
	 * instrumented for this runtime is loaded.
	 * 
	 * @throws Exception
	 *             any internal problem during startup
	 */
	public void startup() throws Exception;

	/**
	 * Allows the coverage runtime to cleanup internals. This class should be
	 * called when classes instrumented for this runtime are not used any more.
	 */
	public void shutdown();

	/**
	 * Before a particular class gets loaded, its execution data structure must
	 * be registered with the runtime through this method. This method must only
	 * be called between {@link #startup()} and {@link #shutdown()}.
	 * 
	 * @param classid
	 *            identifier of the class
	 * @param name
	 *            VM name of the class
	 * @param data
	 *            execution data structure for this class
	 */
	public void registerClass(long classid, final String name, boolean[] data);

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
