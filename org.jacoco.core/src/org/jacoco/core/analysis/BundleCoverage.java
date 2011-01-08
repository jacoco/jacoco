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
package org.jacoco.core.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Coverage data of a bundle. A bundle groups a collection of packages.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class BundleCoverage extends CoverageNodeImpl {

	private final Collection<PackageCoverage> packages;

	/**
	 * Creates a new instance of a bundle with the given name.
	 * 
	 * @param name
	 *            name of this bundle
	 * @param packages
	 *            collection of all packages contained in this bundle
	 */
	public BundleCoverage(final String name,
			final Collection<PackageCoverage> packages) {
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
	public BundleCoverage(final String name,
			final Collection<ClassCoverage> classes,
			final Collection<SourceFileCoverage> sourcefiles) {
		this(name, groupByPackage(classes, sourcefiles));
	}

	/**
	 * Returns all packages contained in this bundle.
	 * 
	 * @return all packages
	 */
	public Collection<PackageCoverage> getPackages() {
		return packages;
	}

	private static Collection<PackageCoverage> groupByPackage(
			final Collection<ClassCoverage> classes,
			final Collection<SourceFileCoverage> sourcefiles) {
		final Map<String, Collection<ClassCoverage>> classesByPackage = new HashMap<String, Collection<ClassCoverage>>();
		for (final ClassCoverage c : classes) {
			addByName(classesByPackage, c.getPackageName(), c);
		}

		final Map<String, Collection<SourceFileCoverage>> sourceFilesByPackage = new HashMap<String, Collection<SourceFileCoverage>>();
		for (final SourceFileCoverage s : sourcefiles) {
			addByName(sourceFilesByPackage, s.getPackageName(), s);
		}

		final Set<String> packageNames = new HashSet<String>();
		packageNames.addAll(classesByPackage.keySet());
		packageNames.addAll(sourceFilesByPackage.keySet());

		final Collection<PackageCoverage> result = new ArrayList<PackageCoverage>();
		for (final String name : packageNames) {
			Collection<ClassCoverage> c = classesByPackage.get(name);
			if (c == null) {
				c = Collections.emptyList();
			}
			Collection<SourceFileCoverage> s = sourceFilesByPackage.get(name);
			if (s == null) {
				s = Collections.emptyList();
			}
			result.add(new PackageCoverage(name, c, s));
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

}
