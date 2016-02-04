/*******************************************************************************
 *  Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import org.jacoco.core.analysis.IAnalyzer;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.tools.ICoverageFetcherStyle;
import org.jacoco.core.tools.LoggingBridge;
import org.junit.Assert;
import org.junit.Test;

/**
 */
public class ReportGeneratorTest {
	@Test
	public void testDefaultConstructor() {
		ReportGenerator instance = new TestReportGenerator();
		assertNotNull(instance.getLog());
		assertEquals(Locale.getDefault(), instance.getLocale());
		assertEquals("JaCoCo Test", instance.getName());
		assertEquals("UTF-8", instance.getSourceEncoding());
		assertEquals("UTF-8", instance.getOutputEncoding());
		assertEquals(new File("target/site/jacoco"),
				instance.getReportOutputDirectory());
		assertEquals(Arrays.asList(new File[] { new File("src/main/java") }),
				instance.getSourceRoots());
		assertEquals(Arrays.asList(new File("target/classes")),
				instance.getClassesDirectories());
		assertFalse(instance.isEBigOEnabled());
		assertNull(instance.getEBigOAttribute());
	}

	@Test
	public void testConstructor() {
		LoggingBridge log = new LoggingBridge() {

			public void warning(String msg) {
			}

			public void severe(String msg) {
			}

			public void info(String msg) {
			}
		};
		ReportGenerator instance = new TestReportGenerator(log);
		assertSame(log, instance.getLog());
	}

	@Test
	public void testSetGetName() {
		ReportGenerator instance = new TestReportGenerator();
		instance.setName("Another Name");
		assertEquals("Another Name", instance.getName());
		instance.setName(null);
		assertEquals("JaCoCo Test", instance.getName());
	}

	@Test
	public void testSetGetSourceEncoding() {
		ReportGenerator instance = new TestReportGenerator();
		instance.setSourceEncoding("UTF-16");
		assertEquals("UTF-16", instance.getSourceEncoding());
		instance.setSourceEncoding(null);
		assertEquals("UTF-8", instance.getSourceEncoding());
	}

	@Test
	public void testSetGetSourceRoots() {
		ReportGenerator instance = new TestReportGenerator();
		instance.setSourceRoots(Arrays.asList(new File[] { new File("src") }));
		assertEquals(Arrays.asList(new File[] { new File("src") }),
				instance.getSourceRoots());
		instance.setSourceRoots(null);
		assertEquals(Arrays.asList(new File[] { new File("src/main/java") }),
				instance.getSourceRoots());
	}

	@Test
	public void testSetGetOutputEncoding() {
		ReportGenerator instance = new TestReportGenerator();
		instance.setOutputEncoding("UTF-16");
		assertEquals("UTF-16", instance.getOutputEncoding());
		instance.setOutputEncoding(null);
		assertEquals("UTF-8", instance.getOutputEncoding());
	}

	@Test
	public void testSetGetReportOutputDirectory() {
		ReportGenerator instance = new TestReportGenerator();
		instance.setReportOutputDirectory(new File("jacoco"));
		assertEquals(new File("jacoco"), instance.getReportOutputDirectory());
		instance.setReportOutputDirectory(null);
		assertEquals(new File("target/site/jacoco"),
				instance.getReportOutputDirectory());
	}

	@Test
	public void testSetGetClassesDirectories() {
		ReportGenerator instance = new TestReportGenerator();
		instance.setClassesDirectories(Arrays.asList(new File[] { new File(
				"bin") }));
		assertEquals(Arrays.asList(new File[] { new File("bin") }),
				instance.getClassesDirectories());
		instance.setClassesDirectories(null);
		assertEquals(Arrays.asList(new File[] { new File("target/classes") }),
				instance.getClassesDirectories());
	}

	@Test
	public void testSetGetLocale() {
		ReportGenerator instance = new TestReportGenerator();
		instance.setLocale(Locale.GERMANY);
		assertEquals(Locale.GERMANY, instance.getLocale());
		instance.setLocale(null);
		assertEquals(Locale.getDefault(), instance.getLocale());
	}

	@Test
	public void testSetGetEBigO_state() {
		ReportGenerator instance = new TestReportGenerator();
		instance.setEBigOEnabled(true);
		assertTrue(instance.isEBigOEnabled());
		assertEquals("DEFAULT", instance.getEBigOAttribute());
		instance.setEBigOEnabled(false);
		assertFalse(instance.isEBigOEnabled());
		assertNull(instance.getEBigOAttribute());
	}

	@Test
	public void testSetGetEBigO_attribute() {
		ReportGenerator instance = new TestReportGenerator();
		instance.setEBigOAttribute("ANOTHER");
		assertTrue(instance.isEBigOEnabled());
		assertEquals("ANOTHER", instance.getEBigOAttribute());
		instance.setEBigOAttribute(null);
		assertFalse(instance.isEBigOEnabled());
		assertNull(instance.getEBigOAttribute());
	}

	@Test
	public void prepReport_cannotGenerate() throws IOException {
		TestReportGenerator instance = new TestReportGenerator();
		instance.prepReport();
		IBundleCoverage bundle = instance.prepReport();
		assertNull(bundle);
		assertEquals(0, instance.getLoadCount());
		assertEquals(0, instance.getAnalyzeCount());
	}

	@Test
	public void prepReport_standard() throws IOException {
		TestReportGenerator instance = new TestReportGenerator();
		instance.setCanGenerate(true);
		IBundleCoverage bundle = instance.prepReport();
		assertEquals(CounterImpl.COUNTER_0_0, bundle.getClassCounter());
		assertEquals(1, instance.getLoadCount());
		assertEquals(1, instance.getAnalyzeCount());
	}

	@Test
	public void prepReport_EBigO() throws IOException {
		TestReportGenerator instance = new TestReportGenerator();
		instance.setCanGenerate(true);
		instance.setEBigOEnabled(true);

		try {
			instance.prepReport();
			Assert.fail("Failed to throw IOException");
		} catch (IOException e) {
			assertEquals(
					"An error has occurred in JaCoCo Test report generation setup.",
					e.getMessage());
			assertEquals(
					"Workload Store does not have enough workloads to calculate a trend (require 4 or more).",
					e.getCause().getMessage());
		}
		assertEquals(1, instance.getLoadCount());
		assertEquals(0, instance.getAnalyzeCount());
	}

	@Test
	public void execute_cannotGenerate() throws Exception {
		TestReportGenerator instance = new TestReportGenerator();
		instance.execute();
	}

	@Test
	public void execute_canGenerate() throws Exception {
		TestReportGenerator instance = new TestReportGenerator();
		instance.setCanGenerate(true);
		File reportOutputDirectory = File.createTempFile("testReport", ".dir");
		try {
			reportOutputDirectory.delete();
			reportOutputDirectory.mkdirs();

			instance.setReportOutputDirectory(reportOutputDirectory);
			instance.execute();
			assertTrue(new File(reportOutputDirectory, "index.html").exists());
			assertTrue(new File(reportOutputDirectory, "jacoco.csv").exists());
			assertTrue(new File(reportOutputDirectory, "jacoco.xml").exists());
		} finally {
			// reportOutputDirectory.delete();
			System.out.println(reportOutputDirectory.getAbsolutePath());
		}
	}

	private static class TestReportGenerator extends ReportGenerator {
		private boolean canGenerate = false;
		private int loadCount = 0;
		private int analyzeCount = 0;

		public TestReportGenerator() {
			super();
		}

		public int getLoadCount() {
			return loadCount;
		}

		public int getAnalyzeCount() {
			return analyzeCount;
		}

		public TestReportGenerator(LoggingBridge log) {
			super(log);
		}

		@Override
		protected void loadExecutionData(ICoverageFetcherStyle fetcher)
				throws IOException {
			loadCount++;
		}

		@Override
		protected void analyzeExecutionData(IAnalyzer analyzer)
				throws IOException {
			analyzeCount++;
		}

		void setCanGenerate(boolean canGenerate) {
			this.canGenerate = canGenerate;
		}

		@Override
		protected boolean canGenerateReport() {
			return canGenerate;
		}

	}
}
