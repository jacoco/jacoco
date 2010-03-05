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
package org.jacoco.core.runtime;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;

/**
 * Base {@link IRuntime} implementation.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public abstract class AbstractRuntime implements IRuntime {

	/** store for execution data */
	protected final ExecutionDataStore store;

	/**
	 * Creates a new runtime.
	 */
	protected AbstractRuntime() {
		store = new ExecutionDataStore();
	}

	public final void collect(final IExecutionDataVisitor visitor,
			final boolean reset) {
		synchronized (store) {
			store.accept(visitor);
			if (reset) {
				store.reset();
			}
		}
	}

	public final void registerClass(final long classid, final String name,
			final boolean[] data) {
		store.put(classid, name, data);
	}

	public final void reset() {
		synchronized (store) {
			store.reset();
		}
	}

}
