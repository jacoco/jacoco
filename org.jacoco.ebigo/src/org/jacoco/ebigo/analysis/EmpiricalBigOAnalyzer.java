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
package org.jacoco.ebigo.analysis;

import static org.jacoco.ebigo.internal.util.ValidationUtils.validateNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.ebigo.core.EmpiricalBigOWorkloadStore;
import org.jacoco.ebigo.core.WorkloadAttributeMap;
import org.jacoco.ebigo.internal.analysis.ClassCoverageIterators;
import org.jacoco.ebigo.internal.analysis.ClassEmpiricalBigOImpl;

/**
 * Perform an Empirical Big-O analysis on workload execution data together with
 * a set of Java class files. The analysis first performs a standard coverage
 * analysis for the classes in each workload and then combines into an empirical
 * Big-O analysis. For each class file the result is reported to a given
 * {@link IEmpiricalBigOVisitor} instance. In addition the
 * {@link EmpiricalBigOAnalyzer} requires a {@link EmpiricalBigOWorkloadStore}
 * instance that holds the execution data for the classes to analyze.
 * <p>
 * TODO: The {@link EmpiricalBigOAnalyzer} offers several methods to analyze
 * classes from a variety of sources.
 * 
 * @author Omer Azmon
 */
public class EmpiricalBigOAnalyzer {
	private final EmpiricalBigOWorkloadStore workloadData;
	private final File classFiles;

	/**
	 * Creates a new Empirical Big-O analyzer reporting to the given output.
	 * 
	 * @param store
	 *            the data store containing the workload execution data to
	 *            analyze
	 * @param classFiles
	 *            file or folder to look for class files
	 * @throws FileNotFoundException
	 *             if {@code classFiles} not found
	 * @throws IllegalArgumentException
	 *             when the Workload Store does not have enough workloads to
	 *             calculate a trend (require 4 or more)
	 */
	public EmpiricalBigOAnalyzer(final EmpiricalBigOWorkloadStore store,
			final File classFiles) throws FileNotFoundException {
		validateNotNull("store", store);
		if (store.size() < 3) {
			throw new IllegalArgumentException(
					"Workload Store does not have enough workloads to calculate a trend (require 4 or more).");
		}
		this.workloadData = store;
		validateclassFiles(classFiles);
		this.classFiles = classFiles;
	}

	private static void validateclassFiles(final File classFiles)
			throws FileNotFoundException {
		validateNotNull("classFiles", classFiles);
		if (!classFiles.exists()) {
			throw new FileNotFoundException(classFiles.getAbsolutePath());
		}
	}

	/**
	 * Returns the workload data provided to the analyzer.
	 * 
	 * @return the workload data provided to the analyzer.
	 */
	public EmpiricalBigOWorkloadStore getWorkloadData() {
		return workloadData;
	}

	/**
	 * Returns the file or folder to look for class files.
	 * 
	 * @return the file or folder to look for class files.
	 */
	public File getClassFiles() {
		return classFiles;
	}

	private List<CoverageBuilder> analyzeAllWorkloadCoverage(
			final XAxisValues xAxisMap, final String attributeName,
			final File classFiles) throws IOException {
		List<CoverageBuilder> analyzerList = new ArrayList<CoverageBuilder>(
				workloadData.size());
		WorkloadAttributeMap[] xKeys = xAxisMap.getXKeys();
		for (int keyIdx = 0; keyIdx < workloadData.size(); keyIdx++) {
			CoverageBuilder coverageVisitor = new CoverageBuilder();
			Analyzer analyzer = new Analyzer(workloadData.get(xKeys[keyIdx])
					.getExecutionDataStore(), coverageVisitor);
			analyzer.analyzeAll(classFiles);
			analyzerList.add(coverageVisitor);
		}
		return analyzerList;
	}

	/**
	 * Analyzes all class files contained in the given file or folder. Class
	 * files as well as ZIP files are considered. Folders are searched
	 * recursively.
	 * 
	 * @param visitor
	 *            the object to receive the results of the analysis
	 * 
	 * @return number of class files found
	 * @throws IOException
	 *             if the file can't be read or a class can't be analyzed
	 */
	public int analyzeAll(IEmpiricalBigOVisitor visitor) throws IOException {
		final String attributeName = visitor.getAttributeName();

		XAxisValues xAxisMap = workloadData.getXAxisValues(attributeName);
		visitor.visitXAxis(xAxisMap);

		validateNotNull("attributeName", attributeName);

		List<CoverageBuilder> analyzerList = analyzeAllWorkloadCoverage(
				xAxisMap, attributeName, classFiles);

		int classCount = 0;

		int[] xValues = xAxisMap.getXValues();
		final ClassCoverageIterators ccsIterators = new ClassCoverageIterators(
				analyzerList);
		while (ccsIterators.hasNext()) {
			final IClassCoverage[] ccs = ccsIterators.next();
			final ClassEmpiricalBigOImpl classEmpiricalBigO = new ClassEmpiricalBigOImpl(ccs);
			classEmpiricalBigO.analyze(visitor.getFitTypes(), xValues);
			visitor.visitEmpiricalBigO(classEmpiricalBigO);
			classCount++;
		}

		return classCount;
	}
}