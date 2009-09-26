/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.report.xml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;

import org.jacoco.core.analysis.BundleCoverage;
import org.jacoco.core.analysis.ClassCoverage;
import org.jacoco.core.analysis.MethodCoverage;
import org.jacoco.core.analysis.PackageCoverage;
import org.jacoco.core.analysis.SourceFileCoverage;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MemoryReportOutput;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link XMLReportFile}
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 * 
 */
public class XMLReportFileTest {

	private MemoryReportOutput output;
	private XMLReportFile report;
	private final ISourceFileLocator nullSourceLocator = new ISourceFileLocator() {

		public Reader getSourceFile(String packageName, String fileName)
				throws IOException {
			return null;
		}
	};

	@Before
	public void setUp() throws Exception {
		output = new MemoryReportOutput();
		report = new XMLReportFile(output, "test.xml");

	}

	@Test
	public void testEmptyReport() throws Exception {
		IReportVisitor groupElement = report.visitChild(new BundleCoverage(
				"test", Arrays.<PackageCoverage> asList()));
		groupElement.visitEnd(nullSourceLocator);
		report.visitEnd(nullSourceLocator);

		assertPathMatches("test", "/report/group/@name");
	}

	@Test
	public void testReportWithNoGroupsOrBundles() throws Exception {

		MethodCoverage methodCoverage = new MethodCoverage("foo", "V", "V");
		ClassCoverage classCoverage = new ClassCoverage("org/jacoco/test/Test",
				"Test.java", Arrays.asList(methodCoverage));
		PackageCoverage packageCoverage = new PackageCoverage(
				"org/jacoco/test", Arrays.asList(classCoverage), Collections
						.<SourceFileCoverage> emptyList());
		BundleCoverage bundleCoverage = new BundleCoverage("test", Arrays
				.asList(packageCoverage));

		IReportVisitor sessionElement = report.visitChild(bundleCoverage);
		IReportVisitor packageElement = sessionElement
				.visitChild(packageCoverage);
		IReportVisitor classElement = packageElement.visitChild(classCoverage);
		IReportVisitor methodElement = classElement.visitChild(methodCoverage);

		methodElement.visitEnd(nullSourceLocator);
		classElement.visitEnd(nullSourceLocator);
		packageElement.visitEnd(nullSourceLocator);
		sessionElement.visitEnd(nullSourceLocator);
		report.visitEnd(nullSourceLocator);

		assertPathMatches("BLOCK",
				"/report/group/package/class/method[@name='foo']/counter/@type");
	}

	private void assertPathMatches(String expected, String path)
			throws Exception {
		XMLSupport support = new XMLSupport(XMLReportFile.class);
		Document document = support.parse(output.getFile("test.xml"));
		assertEquals(expected, support.findStr(document, path));

	}
}
