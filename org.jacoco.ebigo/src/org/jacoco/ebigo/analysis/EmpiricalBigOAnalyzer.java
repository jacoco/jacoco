/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IAnalyzer;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.ebigo.core.EmpiricalBigOWorkloadStore;
import org.jacoco.ebigo.core.WorkloadAttributeMap;
import org.jacoco.ebigo.internal.analysis.ClassCoverageSetIterator;
import org.jacoco.ebigo.internal.analysis.ClassEmpiricalBigOImpl;
import org.objectweb.asm.ClassReader;

/**
 * Perform an Empirical Big-O analysis on workload execution data together with
 * a set of Java class files. The analysis first performs a standard coverage
 * analysis for the classes in each workload and then combines into an empirical
 * Big-O analysis. For each class file the result is reported to a given
 * {@link IEmpiricalBigOVisitor} instance. In addition the
 * {@link EmpiricalBigOAnalyzer} requires a {@link EmpiricalBigOWorkloadStore}
 * instance that holds the execution data for the classes to analyze.
 * 
 * @author Omer Azmon
 */
public class EmpiricalBigOAnalyzer implements IAnalyzer {
	private static final int MAX_SOURCE_FILE_BYTES = 250000;
	private final EmpiricalBigOWorkloadStore workloadData;
	private final IEmpiricalBigOVisitor visitor;
	private final String attributeName;
	private final XAxisValues xAxisMap;

	/**
	 * Creates a new Empirical Big-O analyzer reporting to the given output.
	 * 
	 * @param store
	 *            the data store containing the workload execution data to
	 *            analyze
	 * @param visitor
	 *            the object to receive the results of the analysis
	 * @throws FileNotFoundException
	 *             if {@code classFiles} not found
	 * @throws IllegalArgumentException
	 *             when the Workload Store does not have enough workloads to
	 *             calculate a trend (require 4 or more)
	 */
	public EmpiricalBigOAnalyzer(final EmpiricalBigOWorkloadStore store,
			IEmpiricalBigOVisitor visitor) throws FileNotFoundException {
		validateNotNull("store", store);
		validateNotNull("visitor", visitor);
		if (store.size() < 3) {
			throw new IllegalArgumentException(
					"Workload Store does not have enough workloads to calculate a trend (require 4 or more).");
		}
		this.workloadData = store;
		this.visitor = visitor;
		this.attributeName = visitor.getAttributeName();
		validateNotNull("attributeName", attributeName);
		this.xAxisMap = workloadData.getXAxisValues(attributeName);
		this.visitor.visitXAxis(xAxisMap);
	}

	/**
	 * Returns the workload data provided to the analyzer.
	 * 
	 * @return the workload data provided to the analyzer.
	 */
	public EmpiricalBigOWorkloadStore getWorkloadData() {
		return workloadData;
	}

	public void analyzeClass(final ClassReader reader) throws IOException {
		CoverageSetBuilder setBuilder = new CoverageSetBuilder() {

			@Override
			public void analyzeClassData(IAnalyzer analyzer) throws IOException {
				analyzer.analyzeClass(reader);
			}

		};
		analyzeEBigO(setBuilder.newCoverageBuilderSet());
	}

	public void analyzeClass(final byte[] buffer, final String name)
			throws IOException {
		CoverageSetBuilder setBuilder = new CoverageSetBuilder() {

			@Override
			public void analyzeClassData(IAnalyzer analyzer) throws IOException {
				analyzer.analyzeClass(buffer, name);
			}

		};
		analyzeEBigO(setBuilder.newCoverageBuilderSet());
	}

	public void analyzeClass(final InputStream input, final String name)
			throws IOException {
		CoverageSetBuilder setBuilder = new CoverageSetBuilder() {
			private InputStream markableInputStream = ensureMarkSupported(input);

			@Override
			public void analyzeClassData(IAnalyzer analyzer) throws IOException {
				markableInputStream.mark(MAX_SOURCE_FILE_BYTES);
				analyzer.analyzeClass(input, name);
				markableInputStream.reset();
			}

		};
		analyzeEBigO(setBuilder.newCoverageBuilderSet());
	}

	public int analyzeAll(final InputStream input, final String name)
			throws IOException {
		CoverageSetBuilder setBuilder = new CoverageSetBuilder() {
			private InputStream markableInputStream = ensureMarkSupported(input);

			@Override
			public void analyzeClassData(IAnalyzer analyzer) throws IOException {
				markableInputStream.mark(MAX_SOURCE_FILE_BYTES);
				analyzer.analyzeAll(input, name);
				markableInputStream.reset();
			}

		};
		return analyzeEBigO(setBuilder.newCoverageBuilderSet());
	}

	public int analyzeAll(final String path, final File basedir)
			throws IOException {
		CoverageSetBuilder setBuilder = new CoverageSetBuilder() {

			@Override
			public void analyzeClassData(IAnalyzer analyzer) throws IOException {
				analyzer.analyzeAll(path, basedir);
			}

		};
		return analyzeEBigO(setBuilder.newCoverageBuilderSet());
	}

	public int analyzeAll(final File classFiles) throws IOException {
		validateclassFiles(classFiles);
		CoverageSetBuilder setBuilder = new CoverageSetBuilder() {

			@Override
			public void analyzeClassData(IAnalyzer analyzer) throws IOException {
				analyzer.analyzeAll(classFiles);
			}

		};
		return analyzeEBigO(setBuilder.newCoverageBuilderSet());
	}

	private static byte[] toByteArray(final InputStream input)
			throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final byte[] buffer = new byte[4096];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
		return output.toByteArray();
	}

	private InputStream ensureMarkSupported(InputStream stream)
			throws IOException {
		if (stream.markSupported()) {
			return stream;
		}
		return new ByteArrayInputStream(toByteArray(stream));
	}

	private abstract class CoverageSetBuilder {
		private List<CoverageBuilder> newCoverageBuilderSet()
				throws IOException {
			final List<CoverageBuilder> analyzerList = new ArrayList<CoverageBuilder>(
					workloadData.size());
			final WorkloadAttributeMap[] xKeys = xAxisMap.getXKeys();
			for (int keyIdx = 0; keyIdx < workloadData.size(); keyIdx++) {
				final CoverageBuilder coverageBuilder = new CoverageBuilder();
				final IAnalyzer analyzer = new Analyzer(workloadData.get(
						xKeys[keyIdx]).getExecutionDataStore(), coverageBuilder);
				analyzeClassData(analyzer);
				analyzerList.add(coverageBuilder);
			}
			return analyzerList;
		}

		public abstract void analyzeClassData(IAnalyzer analyzer)
				throws IOException;
	}

	private static void validateclassFiles(final File classFiles)
			throws FileNotFoundException {
		validateNotNull("classFiles", classFiles);
		if (!classFiles.exists()) {
			throw new FileNotFoundException(classFiles.getAbsolutePath());
		}
	}

	private int analyzeEBigO(final List<CoverageBuilder> coverageBuilderSet) {
		int classCount = 0;

		final int[] xValues = xAxisMap.getXValues();
		final ClassCoverageSetIterator classCoverageSetIterators = new ClassCoverageSetIterator(
				coverageBuilderSet);

		while (classCoverageSetIterators.hasNext()) {
			final IClassCoverage[] ccs = classCoverageSetIterators.next();
			final ClassEmpiricalBigOImpl classEmpiricalBigO = new ClassEmpiricalBigOImpl(
					ccs);
			classEmpiricalBigO.analyze(visitor.getFitTypes(), xValues);
			visitor.visitEmpiricalBigO(classEmpiricalBigO);
			classCount++;
		}

		return classCount;
	}
}