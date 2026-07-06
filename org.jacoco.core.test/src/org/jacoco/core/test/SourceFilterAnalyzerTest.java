/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ISourceFileProvider;
import org.jacoco.core.data.ExecutionDataStore;
import org.junit.Before;
import org.junit.Test;

/**
 * Validates that source filtering works when Analyzer is configured with a
 * provider.
 */
public class SourceFilterAnalyzerTest {

	private ExecutionDataStore executionData;
	private CoverageBuilder builder;

	@Before
	public void setup() {
		executionData = new ExecutionDataStore();
		builder = new CoverageBuilder();
	}

	@Test
	public void should_respect_jacoco_off_comments() throws IOException {
		final Map<String, String> sources = new HashMap<String, String>();
		sources.put("org/jacoco/core/test/SourceFilterTarget.java",
				"package org.jacoco.core.test;\n" //
						+ "public class SourceFilterTarget {\n" //
						+ "  public void method() {\n" //
						+ "    //jacoco:off\n" //
						+ "    nop();\n" //
						+ "    //jacoco:on\n" //
						+ "  }\n" //
						+ "  private void nop() {}\n" //
						+ "}\n");

		final ISourceFileProvider provider = new ISourceFileProvider() {
			public Reader getSourceFile(String packageName, String fileName)
					throws IOException {
				String key = packageName + "/" + fileName;
				String content = sources.get(key);
				if (content != null) {
					return new StringReader(content);
				}
				return null;
			}
		};

		Analyzer analyzer = new Analyzer(executionData, builder, provider);
		analyzer.analyzeClass(
				TargetLoader.getClassDataAsBytes(SourceFilterTarget.class),
				"SourceFilterTarget");

		IClassCoverage cc = builder.getClasses().iterator().next();
		// method() has 1 logic line (nop();) inside jacoco:off block.
		// It should be ignored.
		// nop() method itself has logic.

		// Let's check instructions or lines.
		// Without filtering, method() would have covered/missed instructions.
		// With filtering, method() should have 0 instructions covered/missed in
		// the filtered range?

		// Let's verify via line counter on method.
		// "nop();" is on line 6 in the source string above.

		// We expect line 6 to be ignored (EMPTY).
		assertEquals(ICounter.EMPTY,
				cc.getLine(6).getInstructionCounter().getStatus());
	}
}
