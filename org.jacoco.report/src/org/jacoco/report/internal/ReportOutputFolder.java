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
package org.jacoco.report.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.report.IMultiReportOutput;

/**
 * Logical representation of a folder in the output structure. This utility
 * ensures valid and unique file names and helps to create relative links.
 */
public class ReportOutputFolder {

	private final IMultiReportOutput output;

	private final ReportOutputFolder parent;

	private final String path;

	/** Cached sub-folder instances to guarantee stable normalization */
	private final Map<String, ReportOutputFolder> subFolders = new HashMap<String, ReportOutputFolder>();

	private final NormalizedFileNames fileNames;

	/**
	 * Creates a new root folder for the given output.
	 *
	 * @param output
	 *            output for generated files
	 */
	public ReportOutputFolder(final IMultiReportOutput output) {
		this(output, null, "");
	}

	/**
	 * Creates a new root folder for the given output.
	 *
	 * @param output
	 *            output for generated files
	 */
	private ReportOutputFolder(final IMultiReportOutput output,
			final ReportOutputFolder parent, final String path) {
		this.output = output;
		this.parent = parent;
		this.path = path;
		fileNames = new NormalizedFileNames();
	}

	/**
	 * Creates a sub-folder with the given name.
	 *
	 * @param name
	 *            name of the sub-folder
	 * @return handle for output into the sub-folder
	 */
	public ReportOutputFolder subFolder(final String name) {
		final String normalizedName = normalize(name);
		ReportOutputFolder folder = subFolders.get(normalizedName);
		if (folder != null) {
			return folder;
		}
		folder = new ReportOutputFolder(output, this,
				path + normalizedName + "/");
		subFolders.put(normalizedName, folder);
		return folder;
	}

	/**
	 * Creates a new file in this folder with the given local name.
	 *
	 * @param name
	 *            name of the sub-folder
	 * @return handle for output into the sub-folder
	 * @throws IOException
	 *             if the file creation fails
	 */
	public OutputStream createFile(final String name) throws IOException {
		return output.createFile(path + normalize(name));
	}

	/**
	 * Returns a link relative to a given base to a resource within this folder.
	 *
	 * @param base
	 *            base to create the relative link from
	 * @param name
	 *            name of the file or folder in this folder
	 * @return relative link
	 * @throws IllegalArgumentException
	 *             if this folder and the base do not have the same root
	 */
	public String getLink(final ReportOutputFolder base, final String name) {
		if (base.isAncestorOf(this)) {
			return this.path.substring(base.path.length()) + normalize(name);
		}
		if (base.parent == null) {
			throw new IllegalArgumentException("Folders with different roots.");
		}
		return "../" + this.getLink(base.parent, name);
	}

	private boolean isAncestorOf(final ReportOutputFolder folder) {
		if (this == folder) {
			return true;
		}
		return folder.parent == null ? false : isAncestorOf(folder.parent);
	}

	private String normalize(final String name) {
		return fileNames.getFileName(name);
	}

}
