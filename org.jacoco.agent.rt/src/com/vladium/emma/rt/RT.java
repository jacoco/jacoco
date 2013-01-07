/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package com.vladium.emma.rt;

import java.io.File;

/**
 * Compatibility layer for the EMMA runtime which allows to trigger dumps
 * through EMMA APIs.
 * 
 * @deprecated Use {@link org.jacoco.agent.rt.IAgent} instead.
 */
@Deprecated
public final class RT {

	private RT() {
	}

	/**
	 * Triggers an execution data dump for the configured output without
	 * performing a reset.
	 * 
	 * @param outFile
	 *            ignored
	 * @param merge
	 *            ignored
	 * @param stopDataCollection
	 *            ignored
	 * @throws Exception
	 * @see org.jacoco.agent.rt.IAgent#dump(boolean)
	 */
	@SuppressWarnings("unused")
	public static void dumpCoverageData(final File outFile,
			final boolean merge, final boolean stopDataCollection)
			throws Exception {
		org.jacoco.agent.rt.RT.getAgent().dump(false);
	}

	/**
	 * Triggers an execution data dump for the configured output without
	 * performing a reset.
	 * 
	 * @param outFile
	 *            ignored
	 * @param stopDataCollection
	 *            ignored
	 * @throws Exception
	 * @see org.jacoco.agent.rt.IAgent#dump(boolean)
	 */
	public static synchronized void dumpCoverageData(final File outFile,
			final boolean stopDataCollection) throws Exception {
		dumpCoverageData(outFile, true, stopDataCollection);
	}

}
