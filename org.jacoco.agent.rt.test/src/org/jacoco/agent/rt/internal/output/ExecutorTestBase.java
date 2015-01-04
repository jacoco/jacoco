/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt.internal.output;

import static org.junit.Assert.fail;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;

/**
 * Unit tests base for tests that need an {@link Executor} for multithreaded
 * test scenarios.
 */
public abstract class ExecutorTestBase {

	protected ExecutorService executor;

	@Before
	public void setup() throws Exception {
		executor = Executors.newSingleThreadExecutor();
	}

	@After
	public void teardown() throws Exception {
		executor.shutdown();
	}

	/**
	 * Asserts that the given future blocks.
	 * 
	 * @param future
	 *            future to test
	 * @throws Exception
	 */
	protected void assertBlocks(final Future<?> future) throws Exception {
		try {
			future.get(10, TimeUnit.MILLISECONDS);
			fail("Operation should block");
		} catch (TimeoutException e) {
		}
	}

}
