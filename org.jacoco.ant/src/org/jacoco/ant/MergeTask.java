/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.ant;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.util.FileUtils;
import org.jacoco.core.tools.ExecFileLoader;

/**
 * Task for merging a set of execution data files (*.exec) into a single file
 */
public class MergeTask extends Task {

	private File destfile;

	private final Union files = new Union();

	/**
	 * Sets the location of the merged data store
	 *
	 * @param destfile
	 *            Destination data store location
	 */
	public void setDestfile(final File destfile) {
		this.destfile = destfile;
	}

	/**
	 * This task accepts any number of execution data resources.
	 *
	 * @param resources
	 *            Execution data resources
	 */
	public void addConfigured(final ResourceCollection resources) {
		files.add(resources);
	}

	@Override
	public void execute() throws BuildException {
		if (destfile == null) {
			throw new BuildException("Destination file must be supplied",
					getLocation());
		}

		final ExecFileLoader loader = new ExecFileLoader();

		load(loader);
		save(loader);
	}

	private void load(final ExecFileLoader loader) {
		final Iterator<?> resourceIterator = files.iterator();
		while (resourceIterator.hasNext()) {
			final Resource resource = (Resource) resourceIterator.next();

			if (resource.isDirectory()) {
				continue;
			}

			log(format("Loading execution data file %s", resource));

			InputStream resourceStream = null;
			try {
				resourceStream = resource.getInputStream();
				loader.load(resourceStream);
			} catch (final IOException e) {
				throw new BuildException(format("Unable to read %s", resource),
						e, getLocation());
			} finally {
				FileUtils.close(resourceStream);
			}
		}
	}

	private void save(final ExecFileLoader loader) {
		log(format("Writing merged execution data to %s",
				destfile.getAbsolutePath()));
		try {
			loader.save(destfile, false);
		} catch (final IOException e) {
			throw new BuildException(format("Unable to write merged file %s",
					destfile.getAbsolutePath()), e, getLocation());
		}
	}

}
