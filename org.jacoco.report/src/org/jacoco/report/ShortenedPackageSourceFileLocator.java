/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.report;

import java.io.IOException;
import java.io.Reader;

/**
 * This decorator looks for additional file locations with shortened packages
 * when the source file cannot be found at the location of the full package
 * name. This is for programming languages like Kotlin which allow to omit
 * package prefixes in the directory hierarchy.
 */
public class ShortenedPackageSourceFileLocator implements ISourceFileLocator {

	private final ISourceFileLocator delegate;

	/**
	 * Wraps another locator.
	 * 
	 * @param delegate
	 *            original locator which will be used for lookups
	 */
	public ShortenedPackageSourceFileLocator(
			final ISourceFileLocator delegate) {
		this.delegate = delegate;
	}

	public Reader getSourceFile(String packageName, final String fileName)
			throws IOException {
		while (true) {
			final Reader source = delegate.getSourceFile(packageName, fileName);
			if (source != null) {
				return source;
			}
			if (packageName.length() == 0) {
				return null;
			}
			packageName = shortenPackage(packageName);
		}
	}

	private static String shortenPackage(final String pkg) {
		final int sep = pkg.indexOf('/');
		return sep == -1 ? "" : pkg.substring(sep + 1);
	}

	public int getTabWidth() {
		return delegate.getTabWidth();
	}

}
