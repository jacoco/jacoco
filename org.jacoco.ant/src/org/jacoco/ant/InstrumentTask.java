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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.util.FileUtils;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;

/**
 * Task for offline instrumentation of class files.
 */
public class InstrumentTask extends Task {

	private File destdir;

	private final Union files = new Union();

	private boolean removesignatures = true;

	/**
	 * Sets the location of the instrumented classes.
	 *
	 * @param destdir
	 *            destination folder for instrumented classes
	 */
	public void setDestdir(final File destdir) {
		this.destdir = destdir;
	}

	/**
	 * Sets whether signatures should be removed from JAR files.
	 *
	 * @param removesignatures
	 *            <code>true</code> if signatures should be removed
	 */
	public void setRemovesignatures(final boolean removesignatures) {
		this.removesignatures = removesignatures;
	}

	/**
	 * This task accepts any number of class file resources.
	 *
	 * @param resources
	 *            Execution data resources
	 */
	public void addConfigured(final ResourceCollection resources) {
		files.add(resources);
	}

	@Override
	public void execute() throws BuildException {
		if (destdir == null) {
			throw new BuildException("Destination directory must be supplied",
					getLocation());
		}
		int total = 0;
		final Instrumenter instrumenter = new Instrumenter(
				new OfflineInstrumentationAccessGenerator());
		instrumenter.setRemoveSignatures(removesignatures);
		final Iterator<?> resourceIterator = files.iterator();
		while (resourceIterator.hasNext()) {
			final Resource resource = (Resource) resourceIterator.next();
			if (resource.isDirectory()) {
				continue;
			}
			total += instrument(instrumenter, resource);
		}
		log(format("Instrumented %s classes to %s", Integer.valueOf(total),
				destdir.getAbsolutePath()));
	}

	private int instrument(final Instrumenter instrumenter,
			final Resource resource) {
		final File file = new File(destdir, resource.getName());
		file.getParentFile().mkdirs();
		try {
			InputStream input = null;
			OutputStream output = null;
			try {
				input = resource.getInputStream();
				output = new FileOutputStream(file);
				return instrumenter.instrumentAll(input, output,
						resource.getName());
			} finally {
				FileUtils.close(input);
				FileUtils.close(output);
			}
		} catch (final Exception e) {
			file.delete();
			throw new BuildException(
					format("Error while instrumenting %s", resource), e,
					getLocation());
		}
	}
}
