/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.filter;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.test.filter.targets.Synchronized;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.junit.Test;

/**
 * Test of filtering of a bytecode that is generated for a synchronized
 * statement.
 */
public class SynchronizedTest extends ValidationTestBase {

	public SynchronizedTest() {
		super(Synchronized.class);
	}

	/**
	 * {@link Synchronized#normal()}
	 */
	@Test
	public void normal() {
		assertLine("before", ICounter.FULLY_COVERED);
		// when compiled with ECJ next line covered partly without filter:
		assertLine("monitorEnter", ICounter.FULLY_COVERED);
		assertLine("body", ICounter.FULLY_COVERED);
		if (isJDKCompiler) {
			// without filter next line covered partly:
			assertLine("monitorExit", ICounter.FULLY_COVERED);
		} else {
			assertLine("monitorExit", ICounter.EMPTY);
		}
		assertLine("after", ICounter.FULLY_COVERED);
	}

	/**
	 * {@link Synchronized#explicitException()}
	 */
	@Test
	public void explicitException() {
		assertLine("explicitException.monitorEnter", ICounter.FULLY_COVERED);
		assertLine("explicitException.exception", ICounter.FULLY_COVERED);
		// when compiled with javac next line covered fully without filter:
		assertLine("explicitException.monitorExit", ICounter.EMPTY);
	}

	/**
	 * {@link Synchronized#implicitException()}
	 */
	@Test
	public void implicitException() {
		assertLine("implicitException.monitorEnter", isJDKCompiler
				? ICounter.FULLY_COVERED : ICounter.PARTLY_COVERED);
		assertLine("implicitException.exception", ICounter.NOT_COVERED);
		if (isJDKCompiler) {
			// without filter next line covered partly:
			assertLine("implicitException.monitorExit", ICounter.NOT_COVERED);
		} else {
			assertLine("implicitException.monitorExit", ICounter.EMPTY);
		}
	}

}
