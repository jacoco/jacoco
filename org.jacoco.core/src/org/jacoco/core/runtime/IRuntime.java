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
package org.jacoco.core.runtime;

/**
 * This interface represents a particular mechanism to collect execution
 * information in the target VM at runtime.
 */
public interface IRuntime extends IExecutionDataAccessorGenerator {

	/**
	 * Starts the coverage runtime. This method MUST be called before any class
	 * instrumented for this runtime is loaded.
	 * 
	 * @param data
	 *            the execution data for this runtime
	 * @throws Exception
	 *             any internal problem during startup
	 */
	public void startup(RuntimeData data) throws Exception;

	/**
	 * Allows the coverage runtime to cleanup internals. This class should be
	 * called when classes instrumented for this runtime are not used any more.
	 */
	public void shutdown();

}
