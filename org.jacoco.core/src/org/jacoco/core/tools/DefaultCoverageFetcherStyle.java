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
package org.jacoco.core.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IAnalyzer;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

/**
 * Implements the coverage fetch for the standard behavior of JaCoCo.
 */
public class DefaultCoverageFetcherStyle implements ICoverageFetcherStyle {
	private final ExecFileLoader loader;

	/**
	 * Construct a default style instance.
	 */
	public DefaultCoverageFetcherStyle() {
		this.loader = new ExecFileLoader();
	}

	public void loadExecutionData(final File dataFile) throws IOException {
		loader.load(dataFile);
	}

	public void loadExecutionData(final InputStream stream) throws IOException {
		loader.load(stream);
	}

	public SessionInfoStore getSessionInfoStore() {
		return loader.getSessionInfoStore();
	}

	public ExecutionDataStore getExecutionDataStore() {
		return loader.getExecutionDataStore();
	}

	public CoverageBuilder newCoverageBuilder() {
		return new CoverageBuilder();
	}

	public IAnalyzer newAnalyzer(final CoverageBuilder builder) {
		return new Analyzer(getExecutionDataStore(), builder);
	}
}