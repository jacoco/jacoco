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
package org.jacoco.report.check;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jacoco.core.analysis.ICounter.CounterValue;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.ReportStructureTestDriver;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Limit}.
 */
public class RulesCheckerTest implements IViolationsOutput {

	private RulesChecker checker;
	private ReportStructureTestDriver driver;
	private List<String> messages;

	@Before
	public void setup() {
		checker = new RulesChecker();
		driver = new ReportStructureTestDriver();
		messages = new ArrayList<String>();
	}

	@Test
	public void testSetRules() throws IOException {
		Rule rule = new Rule();
		Limit limit = rule.createLimit();
		limit.setValue(CounterValue.MISSEDCOUNT.name());
		limit.setMaximum("5");
		checker.setRules(Arrays.asList(rule));
		driver.sendGroup(checker.createVisitor(this));
		assertEquals(Arrays.asList(
				"Rule violated for bundle bundle: instructions missed count is 10, but expected maximum is 5"),
				messages);
	}

	@Test
	public void testSetLanguageNames() throws IOException {
		Rule rule = new Rule();
		rule.setElement(ElementType.CLASS);
		Limit limit = rule.createLimit();
		limit.setValue(CounterValue.MISSEDCOUNT.name());
		limit.setMaximum("5");
		checker.setRules(Arrays.asList(rule));

		checker.setLanguageNames(new ILanguageNames() {
			public String getQualifiedClassName(String vmname) {
				return "MyClass";
			}

			public String getQualifiedMethodName(String vmclassname,
					String vmmethodname, String vmdesc, String vmsignature) {
				return null;
			}

			public String getPackageName(String vmname) {
				return null;
			}

			public String getMethodName(String vmclassname, String vmmethodname,
					String vmdesc, String vmsignature) {
				return null;
			}

			public String getClassName(String vmname, String vmsignature,
					String vmsuperclass, String[] vminterfaces) {
				return null;
			}
		});

		driver.sendGroup(checker.createVisitor(this));
		assertEquals(Arrays.asList(
				"Rule violated for class MyClass: instructions missed count is 10, but expected maximum is 5"),
				messages);
	}

	public void onViolation(ICoverageNode node, Rule rule, Limit limit,
			String message) {
		messages.add(message);
	}

}
