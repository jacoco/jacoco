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
 *    Dominik Stadler - source folder support
 *
 *******************************************************************************/
package org.jacoco.ant;

import java.util.Iterator;

import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.MultiSourceFileLocator;

/**
 * Source file locator based on Ant resources. The locator supports files as
 * well as directories. The lookup is first performed on files (matching the
 * local file name) and afterwards on directories, by the order the directory
 * resources have been added. The directories are considered as source folders
 * that are searched for source files with the fully qualified name (package and
 * local name).
 */
class AntResourcesLocator extends MultiSourceFileLocator {

	private final String encoding;
	private final AntFilesLocator filesLocator;

	private boolean empty;

	AntResourcesLocator(final String encoding, final int tabWidth) {
		super(tabWidth);
		this.encoding = encoding;
		this.filesLocator = new AntFilesLocator(encoding, tabWidth);
		this.empty = true;
		super.add(filesLocator);
	}

	/**
	 * Adds the given file or directory resource to the locator.
	 *
	 * @param resource
	 *            resource to add
	 */
	void add(final Resource resource) {
		empty = false;
		if (resource.isDirectory()) {
			final FileResource dir = (FileResource) resource;
			super.add(new DirectorySourceFileLocator(dir.getFile(), encoding,
					getTabWidth()));
		} else {
			filesLocator.add(resource);
		}
	}

	void addAll(final Iterator<?> iterator) {
		while (iterator.hasNext()) {
			add((Resource) iterator.next());
		}
	}

	/**
	 * Checks, whether resources have been added.
	 *
	 * @return <code>true</code>, if no resources have been added
	 */
	boolean isEmpty() {
		return empty;
	}

}
