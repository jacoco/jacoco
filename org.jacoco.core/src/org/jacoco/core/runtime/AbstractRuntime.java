/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
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

import java.util.Random;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;

/**
 * Base {@link IRuntime} implementation.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public abstract class AbstractRuntime implements IRuntime {

	/** store for execution data */
	protected final ExecutionDataStore store;

	/** access for this runtime instance */
	protected final ExecutionDataAccess access;

	private long startTimeStamp;

	private String sessionId;

	/**
	 * Creates a new runtime.
	 */
	protected AbstractRuntime() {
		store = new ExecutionDataStore();
		access = new ExecutionDataAccess(store);
		sessionId = generateSessionId();
	}

	/**
	 * Subclasses need to call this method in their {@link #startup()}
	 * implementation to record the timestamp of session startup.
	 */
	protected final void setStartTimeStamp() {
		startTimeStamp = System.currentTimeMillis();
	}

	public void setSessionId(final String id) {
		sessionId = id;
	}

	public String getSessionId() {
		return sessionId;
	}

	public final void collect(final IExecutionDataVisitor executionDataVisitor,
			final ISessionInfoVisitor sessionInfoVisitor, final boolean reset) {
		synchronized (store) {
			if (sessionInfoVisitor != null) {
				final SessionInfo info = new SessionInfo(sessionId,
						startTimeStamp, System.currentTimeMillis());
				sessionInfoVisitor.visitSessionInfo(info);
			}
			store.accept(executionDataVisitor);
			if (reset) {
				reset();
			}
		}
	}

	public final void reset() {
		synchronized (store) {
			store.reset();
			setStartTimeStamp();
		}
	}

	private String generateSessionId() {
		return Integer.toHexString(new Random().nextInt());
	}

}
