/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.csv;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.report.ILanguageNames;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ClassRowWriter}.
 */
public class ClassRowWriterTest {

	private StringWriter result;

	private ClassRowWriter writer;

	@Before
	public void setup() throws Exception {
		ILanguageNames names = new ILanguageNames() {
			public String getClassName(String vmname, String vmsignature,
					String vmsuperclass, String[] vminterfaces) {
				return vmname;
			}

			public String getPackageName(String vmname) {
				return vmname;
			}

			public String getQualifiedClassName(String vmname) {
				throw new AssertionError();
			}

			public String getMethodName(String vmclassname, String vmmethodname,
					String vmdesc, String vmsignature) {
				throw new AssertionError();
			}

			public String getQualifiedMethodName(String vmclassname,
					String vmmethodname, String vmdesc, String vmsignature) {
				throw new AssertionError();
			}
		};
		result = new StringWriter();
		writer = new ClassRowWriter(new DelimitedWriter(result), names);
	}

	@Test
	public void TestHeader() throws Exception {
		BufferedReader reader = getResultReader();
		assertEquals(
				"GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,BRANCH_MISSED,BRANCH_COVERED,LINE_MISSED,LINE_COVERED,COMPLEXITY_MISSED,COMPLEXITY_COVERED,METHOD_MISSED,METHOD_COVERED",
				reader.readLine());
	}

	@Test
	public void TestRow() throws Exception {
		IClassCoverage node = new ClassCoverageImpl("test/package/Foo", 123,
				false) {
			{
				instructionCounter = CounterImpl.getInstance(1, 11);
				branchCounter = CounterImpl.getInstance(2, 22);
				lineCounter = CounterImpl.getInstance(3, 33);
				complexityCounter = CounterImpl.getInstance(4, 44);
				methodCounter = CounterImpl.getInstance(5, 55);
				classCounter = CounterImpl.getInstance(6, 66);
			}
		};
		writer.writeRow("group", "test/package", node);
		BufferedReader reader = getResultReader();
		reader.readLine();
		assertEquals(
				"group,test/package,test/package/Foo,1,11,2,22,3,33,4,44,5,55",
				reader.readLine());
	}

	private BufferedReader getResultReader() {
		return new BufferedReader(new StringReader(result.toString()));
	}

}
