/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.internal.html.page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.ILinkable;

/**
 * Diagnostic information to help users configure their reports correctly.
 */
class DiagnosticInfo {

	private final List<String> infos = new ArrayList<String>();

	private void addInfo(final String info, final Object... args) {
		infos.add(String.format(info, args));
	}

	void checkMissingLineNumbers(final ICoverageNode coverage) {
		if (coverage.getInstructionCounter().getTotalCount() > 0
				&& coverage.getLineCounter().getTotalCount() == 0) {
			addInfo("Class files must be compiled with debug information to show line coverage.");
		}
	}

	void checkSourceReferences(final IPackageCoverage coverage) {
		if (coverage.getInstructionCounter().getTotalCount() > 0
				&& coverage.getSourceFiles().isEmpty()) {
			addInfo("Class files must be compiled with debug information to link with source files.");
		}
	}

	public void checkClassIdMismatch(final IClassCoverage coverage) {
		if (coverage.isNoMatch()) {
			addInfo("A different version of class \"%s\" was executed at runtime.",
					coverage.getName());
		}
	}

	void checkMissingSource(final IClassCoverage coverage,
			final ILinkable sourcePage) {
		final String fileName = coverage.getSourceFileName();
		if (fileName == null) {
			addInfo("Class files must be compiled with debug information to link with source files.\"");
			return;
		}
		if (sourcePage == null) {
			final String sourcePath;
			if (coverage.getPackageName().length() != 0) {
				sourcePath = coverage.getPackageName() + "/" + fileName;
			} else {
				sourcePath = fileName;
			}
			addInfo("Source file \"%s\" was not found during generation of report.",
					sourcePath);
		}
	}

	/**
	 * Adds all identified issues after <code>check*</code> methods have been
	 * called.
	 */
	void renderInfos(final HTMLElement parent) throws IOException {
		for (final String info : infos) {
			parent.p().text(info);
		}
	}

}
