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
package org.jacoco.maven;

import java.io.File;
import java.io.IOException;

import org.jacoco.core.tools.DefaultCoverageFetcherStyle;
import org.jacoco.core.tools.ICoverageFetcherStyle;
import org.jacoco.core.tools.IFetcherStyleProperties;
import org.jacoco.ebigo.tools.EBigOCoverageFetcherStyle;

/**
 * The base class for different methods of fetching execution, session, and
 * coverage data.
 */
public final class MavenCoverageFetcherFactory {
	private MavenCoverageFetcherFactory() {
	}

	/**
	 * A factory to construct a new concrete instance of this class.
	 * 
	 * @param properties
	 *            provides the properties used to determine which concrete
	 *            instance to construct.
	 * @param dataFile
	 *            the source from whence we fetch
	 * @return a new concrete instance of this class.
	 * @throws IOException
	 *             any any failure to read
	 */
	public static final ICoverageFetcherStyle newFetcher(
			final IFetcherStyleProperties properties, final File dataFile)
			throws IOException {
		final ICoverageFetcherStyle dataFetcher = !properties.isEBigOEnabled() ? new DefaultCoverageFetcherStyle(
				properties) : new EBigOCoverageFetcherStyle(properties);
		try {
			dataFetcher.loadExecutionData(dataFile);
		} catch (final IOException e) {
			throw new IOException("Unable to read execution data files in "
					+ dataFile + ": " + e.getMessage(), e);
		}
		return dataFetcher;
	}
}