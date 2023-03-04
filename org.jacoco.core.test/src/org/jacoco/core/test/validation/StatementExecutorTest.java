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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link StatementExecutor}.
 */
public class StatementExecutorTest {

	private Map<String, List<?>> invocations;

	@Before
	public void setup() {
		invocations = new HashMap<String, List<?>>();
	}

	@Test
	public void should_prefix_arguments() {
		StatementExecutor executor = new StatementExecutor(this, "Hello",
				"world");

		executor.visitInvocation("ctx", "target1", "!");

		assertEquals(Arrays.asList("Hello", "world", "!"),
				invocations.get("target1"));
	}

	@Test
	public void should_call_method_with_int_argument() {
		StatementExecutor executor = new StatementExecutor(this);

		executor.visitInvocation("ctx", "target2", Integer.valueOf(42));

		assertEquals(Arrays.asList(Integer.valueOf(42)),
				invocations.get("target2"));
	}

	@Test
	public void should_preserve_AssertionError() {
		StatementExecutor executor = new StatementExecutor(this);
		try {
			executor.visitInvocation("ctx", "target3");
			fail("exception expected");
		} catch (AssertionError e) {
			assertEquals("Original AssertionError.", e.getMessage());
		}
	}

	@Test
	public void should_wrap_other_exceptions() {
		StatementExecutor executor = new StatementExecutor(this);
		try {
			executor.visitInvocation("ctx", "target4");
			fail("exception expected");
		} catch (RuntimeException e) {
			assertEquals("Invocation error (ctx)", e.getMessage());
			assertEquals("Original IOException.", e.getCause().getMessage());
		}
	}

	@Test
	public void should_throw_RuntimeException_when_method_cannot_be_invoked() {
		StatementExecutor executor = new StatementExecutor(this);
		try {
			executor.visitInvocation("ctx", "doesNotExist");
			fail("exception expected");
		} catch (RuntimeException e) {
			assertEquals("Invocation error (ctx)", e.getMessage());
		}
	}

	public void target1(String a, String b, String c) {
		invocations.put("target1", Arrays.asList(a, b, c));
	}

	public void target2(int i) {
		invocations.put("target2", Arrays.asList(Integer.valueOf(i)));
	}

	public void target3() {
		throw new AssertionError("Original AssertionError.");
	}

	public void target4() throws IOException {
		throw new IOException("Original IOException.");
	}

}
