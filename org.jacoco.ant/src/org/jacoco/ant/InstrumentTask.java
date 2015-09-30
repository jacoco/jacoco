/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.jacoco.core.data.ProbeMode;
import org.jacoco.core.instr.InstrumentationConfig;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;

/**
 * Task for offline instrumentation of class files.
 */
public class InstrumentTask extends Task {

	private File destdir;

	private final Union files = new Union();

	private boolean removesignatures = true;

	private ProbeMode probeMode = ProbeMode.exists;

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
	 * Sets the probe mode to use form instrumentation. Default is
	 * <code>exists</code>
	 * 
	 * @param probeMode
	 *            Probe mode to use for instrumentation
	 */
	public void setProbe(final ProbeMode probeMode) {
		this.probeMode = probeMode != null ? probeMode : ProbeMode.exists;
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
		InstrumentationConfig.reset();
		InstrumentationConfig.configure(this.probeMode);

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
		log(format("Instrumented %s classes to %s in mode '%s'",
				Integer.valueOf(total), destdir.getAbsolutePath(),
				InstrumentationConfig.getProbeMode()));
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
			throw new BuildException(format("Error while instrumenting %s",
					resource), e, getLocation());
		}
	}
}
