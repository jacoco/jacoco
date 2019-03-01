/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit tests for {@link StatementExecutor}.
 */
public class StatementExecutorTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private Map<String, List<?>> invocations;

	@Before
	public void setup() {
		invocations = new HashMap<String, List<?>>();
	}

	@Test
	public void should_prefix_arguments() {
		StatementExecutor executor = new StatementExecutor(this,
				"Hello", "world");

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
		exception.expect(AssertionError.class);
		exception.expectMessage("Original AssertionError.");
		StatementExecutor executor = new StatementExecutor(this);

		executor.visitInvocation("ctx", "target3");
	}

	@Test
	public void should_wrap_other_exceptions() {
		exception.expect(RuntimeException.class);
		exception.expectMessage("Invocation error (ctx)");
		StatementExecutor executor = new StatementExecutor(this);

		executor.visitInvocation("ctx", "target4");
	}

	@Test
	public void should_throw_RuntimeException_when_method_cannot_be_invoked() {
		exception.expect(RuntimeException.class);
		exception.expectMessage("Invocation error (ctx)");
		StatementExecutor executor = new StatementExecutor(this);

		executor.visitInvocation("ctx", "doesNotExist");
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
