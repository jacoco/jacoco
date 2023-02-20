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
package org.jacoco.report.check;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.JavaNames;

/**
 * Formatter which checks a set of given rules and reports violations to a
 * {@link IViolationsOutput} instance.
 */
public class RulesChecker {

	private List<Rule> rules;
	private ILanguageNames languageNames;

	/**
	 * New formatter instance.
	 */
	public RulesChecker() {
		this.rules = new ArrayList<Rule>();
		this.setLanguageNames(new JavaNames());
	}

	/**
	 * Sets the rules to check by this formatter.
	 *
	 * @param rules
	 *            rules to check
	 */
	public void setRules(final List<Rule> rules) {
		this.rules = rules;
	}

	/**
	 * Sets the implementation for language name display for message formatting.
	 * Java language names are defined by default.
	 *
	 * @param languageNames
	 *            converter for language specific names
	 */
	public void setLanguageNames(final ILanguageNames languageNames) {
		this.languageNames = languageNames;
	}

	/**
	 * Creates a new visitor to process the configured checks.
	 *
	 * @param output
	 *            call-back to report violations to
	 * @return visitor to emit the report data to
	 */
	public IReportVisitor createVisitor(final IViolationsOutput output) {
		final BundleChecker bundleChecker = new BundleChecker(rules,
				languageNames, output);
		return new IReportVisitor() {

			public IReportGroupVisitor visitGroup(final String name)
					throws IOException {
				return this;
			}

			public void visitBundle(final IBundleCoverage bundle,
					final ISourceFileLocator locator) throws IOException {
				bundleChecker.checkBundle(bundle);
			}

			public void visitInfo(final List<SessionInfo> sessionInfos,
					final Collection<ExecutionData> executionData)
					throws IOException {
			}

			public void visitEnd() throws IOException {
			}
		};
	}

}
