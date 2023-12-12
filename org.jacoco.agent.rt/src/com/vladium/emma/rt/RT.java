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
package com.vladium.emma.rt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Compatibility layer for the EMMA runtime which allows to trigger dumps
 * through EMMA APIs. Note that even this class emulates an EMMA API the files
 * written are in JaCoCo execution data format.
 *
 * @deprecated Use {@link org.jacoco.agent.rt.IAgent} instead.
 */
@Deprecated
public final class RT {

	private RT() {
	}

	/**
	 * Writes the current execution data to the given file in JaCoCo execution
	 * data format.
	 *
	 * @param outFile
	 *            file to write execution data to
	 * @param merge
	 *            if <code>true</code>, execution data is appended to an
	 *            existing file
	 * @param stopDataCollection
	 *            ignored
	 * @throws IOException
	 *             in case of problems with the file output
	 */
	@SuppressWarnings("unused")
	public static void dumpCoverageData(final File outFile, final boolean merge,
			final boolean stopDataCollection) throws IOException {
		final OutputStream out = new FileOutputStream(outFile, merge);
		try {
			out.write(
					org.jacoco.agent.rt.RT.getAgent().getExecutionData(false));
		} finally {
			out.close();
		}
	}

	/**
	 * Writes the current execution data to the given file in JaCoCo execution
	 * data format. If the file already exists new data is appended.
	 *
	 * @param outFile
	 *            file to write execution data to
	 * @param stopDataCollection
	 *            ignored
	 * @throws IOException
	 *             in case of problems with the file output
	 */
	public static synchronized void dumpCoverageData(final File outFile,
			final boolean stopDataCollection) throws IOException {
		dumpCoverageData(outFile, true, stopDataCollection);
	}

}
