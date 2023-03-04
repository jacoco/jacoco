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
package org.jacoco.core.test.perf.targets;

import java.util.concurrent.Callable;

/**
 * Simple Loop.
 */
public class Target02 implements Callable<Void> {

	public Void call() throws Exception {
		@SuppressWarnings("unused")
		int count = 0;
		for (int i = 0; i < 10000000; i++) {
			count++;
		}
		return null;
	}

}
