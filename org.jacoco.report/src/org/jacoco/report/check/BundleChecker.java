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

import java.util.ArrayList;
import java.util.Collection;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.report.ILanguageNames;

/**
 * Internal class to check a list of rules against a {@link IBundleCoverage}
 * instance.
 */
class BundleChecker {

	private final ILanguageNames names;
	private final IViolationsOutput output;

	private final Collection<Rule> bundleRules;
	private final Collection<Rule> packageRules;
	private final Collection<Rule> classRules;
	private final Collection<Rule> sourceFileRules;
	private final Collection<Rule> methodRules;

	private final boolean traversePackages;
	private final boolean traverseClasses;
	private final boolean traverseSourceFiles;
	private final boolean traverseMethods;

	public BundleChecker(final Collection<Rule> rules,
			final ILanguageNames names, final IViolationsOutput output) {
		this.names = names;
		this.output = output;
		this.bundleRules = new ArrayList<Rule>();
		this.packageRules = new ArrayList<Rule>();
		this.classRules = new ArrayList<Rule>();
		this.sourceFileRules = new ArrayList<Rule>();
		this.methodRules = new ArrayList<Rule>();
		for (final Rule rule : rules) {
			switch (rule.getElement()) {
			case BUNDLE:
				bundleRules.add(rule);
				break;
			case PACKAGE:
				packageRules.add(rule);
				break;
			case CLASS:
				classRules.add(rule);
				break;
			case SOURCEFILE:
				sourceFileRules.add(rule);
				break;
			case METHOD:
				methodRules.add(rule);
				break;
			}
		}
		traverseMethods = !methodRules.isEmpty();
		traverseClasses = !classRules.isEmpty() || traverseMethods;
		traverseSourceFiles = !sourceFileRules.isEmpty();
		traversePackages = !packageRules.isEmpty() || traverseClasses
				|| traverseSourceFiles;
	}

	public void checkBundle(final IBundleCoverage bundleCoverage) {
		final String name = bundleCoverage.getName();
		checkRules(bundleCoverage, bundleRules, "bundle", name);
		if (traversePackages) {
			for (final IPackageCoverage p : bundleCoverage.getPackages()) {
				check(p);
			}
		}
	}

	private void check(final IPackageCoverage packageCoverage) {
		final String name = names.getPackageName(packageCoverage.getName());
		checkRules(packageCoverage, packageRules, "package", name);
		if (traverseClasses) {
			for (final IClassCoverage c : packageCoverage.getClasses()) {
				check(c);
			}
		}
		if (traverseSourceFiles) {
			for (final ISourceFileCoverage s : packageCoverage
					.getSourceFiles()) {
				check(s);
			}
		}
	}

	private void check(final IClassCoverage classCoverage) {
		final String name = names
				.getQualifiedClassName(classCoverage.getName());
		checkRules(classCoverage, classRules, "class", name);
		if (traverseMethods) {
			for (final IMethodCoverage m : classCoverage.getMethods()) {
				check(m, classCoverage.getName());
			}
		}
	}

	private void check(final ISourceFileCoverage sourceFile) {
		final String name = sourceFile.getPackageName() + "/"
				+ sourceFile.getName();
		checkRules(sourceFile, sourceFileRules, "source file", name);
	}

	private void check(final IMethodCoverage method, final String className) {
		final String name = names.getQualifiedMethodName(className,
				method.getName(), method.getDesc(), method.getSignature());
		checkRules(method, methodRules, "method", name);
	}

	private void checkRules(final ICoverageNode node,
			final Collection<Rule> rules, final String typename,
			final String elementname) {
		for (final Rule rule : rules) {
			if (rule.matches(elementname)) {
				for (final Limit limit : rule.getLimits()) {
					checkLimit(node, typename, elementname, rule, limit);
				}
			}
		}
	}

	private void checkLimit(final ICoverageNode node, final String elementtype,
			final String elementname, final Rule rule, final Limit limit) {
		final String message = limit.check(node);
		if (message != null) {
			output.onViolation(node, rule, limit,
					String.format("Rule violated for %s %s: %s", elementtype,
							elementname, message));
		}
	}

}
