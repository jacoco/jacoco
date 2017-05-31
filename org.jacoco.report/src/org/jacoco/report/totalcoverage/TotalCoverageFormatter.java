/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Maurice Quach - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.totalcoverage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;

/**
 * Report formatter that will create a single text file containing the coverage
 * percentage.
 */
public class TotalCoverageFormatter {
	private String outputEncoding = "UTF-8";

	/**
	 * Sets the encoding used for generated document. Default is UTF-8.
	 * 
	 * @param outputEncoding
	 *            output encoding
	 */
	public void setOutputEncoding(final String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	/**
	 * Creates a new visitor to write a report to the given stream.
	 * 
	 * @param output
	 *            output stream to write the report to
	 * @return visitor to emit the report data to
	 * @throws IOException
	 *             in case of problems with the output stream
	 */
	public IReportVisitor createVisitor(final OutputStream output)
			throws IOException {
		final Writer writer = new OutputStreamWriter(output, outputEncoding);

		class Visitor implements IReportVisitor {
			public void visitInfo(final List<SessionInfo> sessionInfos,
					final Collection<ExecutionData> executionData)
					throws IOException {
				// Info not used
			}

			public void visitEnd() throws IOException {
				writer.close();
			}

			public void visitBundle(final IBundleCoverage bundle,
					final ISourceFileLocator locator) throws IOException {
				final ICounter instructionCounter = bundle.getInstructionCounter();
				final String percentage = String.valueOf(
						Math.floor(
								(instructionCounter != null ? 
										((instructionCounter.getMissedRatio() +
												instructionCounter.getCoveredCount() == 0) ?
												0 :
												instructionCounter.getCoveredRatio())
										: 0)
								* 10000) / 100);
				writer.write(percentage);
			}

			public IReportGroupVisitor visitGroup(final String name)
					throws IOException {
				return null;
			}
		}
		return new Visitor();
	}
}
