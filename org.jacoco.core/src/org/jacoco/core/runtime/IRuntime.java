/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
	void startup(RuntimeData data) throws Exception;

	/**
	 * Allows the coverage runtime to cleanup internals. This class should be
	 * called when classes instrumented for this runtime are not used any more.
	 */
	void shutdown();

}
