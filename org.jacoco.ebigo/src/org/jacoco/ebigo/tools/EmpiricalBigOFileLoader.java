/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.ebigo.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import org.jacoco.ebigo.core.EmpiricalBigOWorkload;
import org.jacoco.ebigo.core.EmpiricalBigOWorkloadStore;
import org.jacoco.ebigo.core.WorkloadAttributeMapBuilder;

import static org.jacoco.ebigo.internal.util.ValidationUtils.*;

/**
 * 
 */
public class EmpiricalBigOFileLoader {
	private final EmpiricalBigOWorkloadStore store;

	/**
	 * Constructor using
	 * <code>WorkloadAttributeMapBuilder.DEFAULT_ATTRIBUTE</code>
	 */
	public EmpiricalBigOFileLoader() {
		this(null);
	}

	/**
	 * Constructor
	 * 
	 * @param requiredAttribute
	 *            this attribute MUST be defined in the attached attribute map
	 *            file of any execution data file loaded. If {@code null}, it is
	 *            set to
	 *            <code>WorkloadAttributeMapBuilder.DEFAULT_ATTRIBUTE</code>
	 */
	public EmpiricalBigOFileLoader(final String requiredAttribute) {
		store = new EmpiricalBigOWorkloadStore(
				requiredAttribute == null ? WorkloadAttributeMapBuilder.DEFAULT_ATTRIBUTE
						: requiredAttribute);
	}

	/**
	 * Load all workload execution data and attribute maps in a directory. This
	 * method may be invoked multiple times with different directories to
	 * aggregate data from multiple locations.
	 * 
	 * @param dataFile
	 *            the name of the directory to search for execution data or
	 *            attribute maps. This can also be the name of an execution data
	 *            file. In that case, its directory is searched.
	 * @throws IOException
	 *             on any failure to read.
	 */
	public void load(final File dataFile) throws IOException {
		validateNotNull("dataFile", dataFile);
		final File dataDir = dataFile.isDirectory() ? dataFile : dataFile
				.getParentFile();

		final String[] workloadFilelist = dataDir.list(new FilenameFilter() {
			public boolean accept(final File dir, final String name) {
				return (dir.equals(dataDir) && name.endsWith(".exec"));
			}
		});
		if (workloadFilelist == null) {
			throw new FileNotFoundException(
					"Unable to find any data file in or around " + dataFile);
		}

		for (final String workloadFilename : workloadFilelist) {
			String filename = workloadFilename
					.substring(0, workloadFilename.length() - 5);
			store.put(EmpiricalBigOWorkload.read(dataDir, filename));
		}
	}

	public void load(InputStream stream) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the Empirical-Big-O Workload Store. Invoking this method multiple
	 * times returns the same store.
	 * 
	 * @return the Empirical-Big-O Workload Store.
	 */
	public EmpiricalBigOWorkloadStore getWorkloadstore() {
		return store;
	}

}