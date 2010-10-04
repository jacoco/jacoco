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
package org.jacoco.core.runtime;

import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;

/**
 * This interface represents a particular mechanism to collect execution
 * information in the target VM at runtime.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public interface IRuntime extends IExecutionDataAccessorGenerator {

	/**
	 * Sets a session identifier for this runtime. The identifier is used when
	 * execution data is collected. If no identifier is explicitly set a
	 * identifier is generated from the host name and a random number. This
	 * method can be called at any time.
	 * 
	 * @see #collect(IExecutionDataVisitor, ISessionInfoVisitor, boolean)
	 * @param id
	 *            new session identifier
	 */
	public void setSessionId(String id);

	/**
	 * Get the current a session identifier for this runtime.
	 * 
	 * @see #setSessionId(String)
	 * @return current session identifier
	 */
	public String getSessionId();

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
	 * Collects the current execution data and writes it to the given
	 * {@link IExecutionDataVisitor} object. This method must only be called
	 * between {@link #startup()} and {@link #shutdown()}.
	 * 
	 * @param executionDataVisitor
	 *            handler to write coverage data to
	 * @param sessionInfoVisitor
	 *            optional visitor to write session information to or
	 *            <code>null</code> if session information is not required
	 * @param reset
	 *            if <code>true</code> the current coverage information is also
	 *            cleared
	 */
	public void collect(IExecutionDataVisitor executionDataVisitor,
			ISessionInfoVisitor sessionInfoVisitor, boolean reset);

	/**
	 * Resets all coverage information. This method must only be called between
	 * {@link #startup()} and {@link #shutdown()}.
	 */
	public void reset();

}
