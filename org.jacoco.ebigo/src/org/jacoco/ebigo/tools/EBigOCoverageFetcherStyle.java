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
import java.io.IOException;
import java.io.InputStream;

import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IAnalyzer;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.tools.ICoverageFetcherStyle;
import org.jacoco.core.tools.IFetcherStyleProperties;
import org.jacoco.ebigo.analysis.EmpiricalBigOAnalyzer;
import org.jacoco.ebigo.analysis.EmpiricalBigOBuilder;
import org.jacoco.ebigo.core.EmpiricalBigOWorkloadStore;
import org.jacoco.ebigo.fit.FitType;

/**
 * Implements the coverage fetch for performing E-BigO analysis in addition to
 * the standard analysis.
 */
public class EBigOCoverageFetcherStyle implements ICoverageFetcherStyle {
	protected final EmpiricalBigOFileLoader loader;

	/**
	 * Construct a E-BigO style instance.
	 * 
	 * @param properties
	 *            the properties needed for the style operations
	 */
	public EBigOCoverageFetcherStyle(final IFetcherStyleProperties properties) {
		loader = new EmpiricalBigOFileLoader(properties.getEBigOAttribute());
	}

	public void loadExecutionData(File dataFile) throws IOException {
		loader.load(dataFile);
	}

	public void loadExecutionData(final InputStream stream) throws IOException {
		loader.load(stream);
	}

	public SessionInfoStore getSessionInfoStore() {
		EmpiricalBigOWorkloadStore workloadStore = loader.getWorkloadstore();
		if (workloadStore == null) {
			return null;
		}
		return workloadStore.getMergedSessionInfoStore();
	}

	public ExecutionDataStore getExecutionDataStore() {
		EmpiricalBigOWorkloadStore workloadStore = loader.getWorkloadstore();
		if (workloadStore == null) {
			return null;
		}
		return workloadStore.getMergedExecutionDataStore();
	}

	public CoverageBuilder newCoverageBuilder() {
		EmpiricalBigOWorkloadStore workloadStore = loader.getWorkloadstore();
		return new EmpiricalBigOBuilder(FitType.values(),
				workloadStore.getDefaultAttribute());
	}

	public IAnalyzer newAnalyzer(CoverageBuilder builder) throws IOException {
		if (builder instanceof EmpiricalBigOBuilder) {
			EmpiricalBigOWorkloadStore workloadStore = loader.getWorkloadstore();
			EmpiricalBigOBuilder ebigoBuilder = (EmpiricalBigOBuilder) builder;
			return new EmpiricalBigOAnalyzer(workloadStore, ebigoBuilder);
		}
		throw new IllegalArgumentException(
				"'builder' is not generated by this implementation.");
	}
}