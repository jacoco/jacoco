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
package org.jacoco.core.runtime;

import java.util.Random;

/**
 * Base {@link IRuntime} implementation.
 */
public abstract class AbstractRuntime implements IRuntime {

	/** access to the runtime data */
	protected RuntimeData data;

	/**
	 * Subclasses must call this method when overwriting it.
	 */
	public void startup(final RuntimeData data) throws Exception {
		this.data = data;
	}

	private static final Random RANDOM = new Random();

	/**
	 * Creates a random session identifier.
	 *
	 * @return random session identifier
	 */
	public static String createRandomId() {
		return Integer.toHexString(RANDOM.nextInt());
	}

}
