/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.analysis.CoverageNodeImpl;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.tools.LoggingBridge;

/**
 * Implementation of {@link IBundleCoverage}.
 */
public class BundleCoverageImpl extends CoverageNodeImpl implements
		IBundleCoverage {

	private final Collection<IPackageCoverage> packages;

	/**
	 * Creates a new instance of a bundle with the given name.
	 * 
	 * @param name
	 *            name of this bundle
	 * @param packages
	 *            collection of all packages contained in this bundle
	 */
	public BundleCoverageImpl(final String name,
			final Collection<IPackageCoverage> packages) {
		super(ElementType.BUNDLE, name);
		this.packages = packages;
		increment(packages);
	}

	/**
	 * Creates a new instance of a bundle with the given name. The packages are
	 * calculated from the given classes and source files.
	 * 
	 * @param name
	 *            name of this bundle
	 * @param classes
	 *            all classes in this bundle
	 * @param sourcefiles
	 *            all source files in this bundle
	 */
	public BundleCoverageImpl(final String name,
			final Collection<IClassCoverage> classes,
			final Collection<ISourceFileCoverage> sourcefiles) {
		this(name, groupByPackage(classes, sourcefiles));
	}

	private static Collection<IPackageCoverage> groupByPackage(
			final Collection<IClassCoverage> classes,
			final Collection<ISourceFileCoverage> sourcefiles) {
		final Map<String, Collection<IClassCoverage>> classesByPackage = new HashMap<String, Collection<IClassCoverage>>();
		for (final IClassCoverage c : classes) {
			addByName(classesByPackage, c.getPackageName(), c);
		}

		final Map<String, Collection<ISourceFileCoverage>> sourceFilesByPackage = new HashMap<String, Collection<ISourceFileCoverage>>();
		for (final ISourceFileCoverage s : sourcefiles) {
			addByName(sourceFilesByPackage, s.getPackageName(), s);
		}

		final Set<String> packageNames = new HashSet<String>();
		packageNames.addAll(classesByPackage.keySet());
		packageNames.addAll(sourceFilesByPackage.keySet());

		final Collection<IPackageCoverage> result = new ArrayList<IPackageCoverage>();
		for (final String name : packageNames) {
			Collection<IClassCoverage> c = classesByPackage.get(name);
			if (c == null) {
				c = Collections.emptyList();
			}
			Collection<ISourceFileCoverage> s = sourceFilesByPackage.get(name);
			if (s == null) {
				s = Collections.emptyList();
			}
			result.add(new PackageCoverageImpl(name, c, s));
		}
		return result;
	}

	private static <T> void addByName(final Map<String, Collection<T>> map,
			final String name, final T value) {
		Collection<T> list = map.get(name);
		if (list == null) {
			list = new ArrayList<T>();
			map.put(name, list);
		}
		list.add(value);
	}

	// === IBundleCoverage implementation ===

	public Collection<IPackageCoverage> getPackages() {
		return packages;
	}

	public void logCoverageInfo(
			final Collection<IClassCoverage> noMatchClasses,
			final LoggingBridge log) {
		log.info(format("Analyzed bundle '%s' with %s classes", getName(),
				Integer.valueOf(getClassCounter().getTotalCount())));
		if (!noMatchClasses.isEmpty()) {
			log.warning(format(
					"Classes in bundle '%s' do no match with execution data. "
							+ "For report generation the same class files must be used as at runtime.",
					getName()));
			for (final IClassCoverage c : noMatchClasses) {
				log.warning(format(
						"Execution data for class %s does not match.",
						c.getName()));
			}
		}
	}

	public void logMissingDebugInformation(final LoggingBridge log) {
		if (getClassCounter().getTotalCount() > 0
				&& getLineCounter().getTotalCount() == 0) {
			log.warning(format(
					"To enable source code annotation class files for bundle '%s' have to be compiled with debug information.",
					getName()));
		}
	}

}
