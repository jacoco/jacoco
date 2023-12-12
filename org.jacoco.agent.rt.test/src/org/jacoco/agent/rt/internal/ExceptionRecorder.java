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
package org.jacoco.agent.rt.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * {@link IExceptionLogger} implementation for testing purposes.
 */
public class ExceptionRecorder implements IExceptionLogger {

	private Class<?> exceptionType;
	private String message;
	private Class<?> causeType;

	public void logExeption(Exception ex) {
		assertNull("multiple exeptions", exceptionType);
		exceptionType = ex.getClass();
		message = ex.getMessage();
		causeType = ex.getCause() == null ? null : ex.getCause().getClass();
	}

	public void clear() {
		exceptionType = null;
		message = null;
		causeType = null;
	}

	public void assertNoException() {
		assertNull(exceptionType);
	}

	public void assertException(final Class<? extends Throwable> exceptionType,
			final String message) {
		assertEquals(exceptionType, this.exceptionType);
		assertEquals(message, this.message);
	}

	public void assertException(final Class<? extends Throwable> exceptionType,
			final String message, final Class<? extends Throwable> causeType) {
		assertEquals(exceptionType, this.exceptionType);
		assertEquals(message, this.message);
		assertEquals(causeType, this.causeType);
	}

}
