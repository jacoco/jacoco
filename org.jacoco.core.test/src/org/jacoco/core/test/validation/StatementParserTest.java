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
package org.jacoco.core.test.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jacoco.core.test.validation.StatementParser.IStatementVisitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link StatementParser}
 */
public class StatementParserTest {

	private IStatementVisitor visitor;

	private List<String> actualInvocations;
	private List<String> expectedInvocations;

	@Before
	public void setup() {
		actualInvocations = new ArrayList<String>();
		expectedInvocations = new ArrayList<String>();
		visitor = new IStatementVisitor() {
			public void visitInvocation(String ctx, String name,
					Object... args) {
				actualInvocations.add(invocationStr(ctx, name, args));
			}
		};
	}

	@After
	public void teardown() {
		assertEquals(expectedInvocations, actualInvocations);
	}

	@Test
	public void should_parse_empty_string() throws IOException {
		StatementParser.parse("", visitor, "Foo.java");
	}

	@Test
	public void should_parse_invocation_without_params() throws IOException {
		StatementParser.parse("run()", visitor, "Foo.java");
		expectInvocation("Foo.java", "run");
	}

	@Test
	public void should_parse_invocation_with_one_int_parameter()
			throws IOException {
		StatementParser.parse("ask(42)", visitor, "Foo.java");
		expectInvocation("Foo.java", "ask", Integer.valueOf(42));
	}

	@Test
	public void should_parse_invocation_with_one_string_parameter()
			throws IOException {
		StatementParser.parse("say(\"hello\")", visitor, "Foo.java");
		expectInvocation("Foo.java", "say", "hello");
	}

	@Test
	public void should_parse_invocation_with_two_parameters()
			throws IOException {
		StatementParser.parse("add(1000, 234)", visitor, "Foo.java");
		expectInvocation("Foo.java", "add", Integer.valueOf(1000),
				Integer.valueOf(234));
	}

	@Test
	public void should_parse_invocation_with_mixed_parameter_types()
			throws IOException {
		StatementParser.parse("mix(1, \"two\", 3)", visitor, "Foo.java");
		expectInvocation("Foo.java", "mix", Integer.valueOf(1), "two",
				Integer.valueOf(3));
	}

	@Test
	public void should_parse_multiple_invocations() throws IOException {
		StatementParser.parse("start() stop()", visitor, "Foo.java");
		expectInvocation("Foo.java", "start");
		expectInvocation("Foo.java", "stop");
	}

	@Test
	public void should_fail_when_parenthesis_is_missing() throws IOException {
		try {
			StatementParser.parse("bad(", visitor, "Foo.java");
			fail("exception expected");
		} catch (IOException e) {
			// expected
		}
	}

	@Test
	public void should_fail_when_argument1_is_missing() throws IOException {
		try {
			StatementParser.parse("bad(,2)", visitor, "Foo.java");
			fail("exception expected");
		} catch (IOException e) {
			// expected
		}
	}

	@Test
	public void should_fail_when_argument2_is_missing() throws IOException {
		try {
			StatementParser.parse("bad(1,)", visitor, "Foo.java");
			fail("exception expected");
		} catch (IOException e) {
			// expected
		}
	}

	@Test
	public void should_give_context_info_when_parsing_fails()
			throws IOException {
		try {
			StatementParser.parse("bad", visitor, "Foo.java:32");
			fail("exception expected");
		} catch (IOException e) {
			assertEquals("Invalid syntax (Foo.java:32)", e.getMessage());
		}
	}

	private void expectInvocation(String ctx, String name, Object... args) {
		expectedInvocations.add(invocationStr(ctx, name, args));
	}

	private String invocationStr(String ctx, String name, Object... args) {
		return String.format("%s:%s%s", ctx, name, Arrays.asList(args));
	}

}
