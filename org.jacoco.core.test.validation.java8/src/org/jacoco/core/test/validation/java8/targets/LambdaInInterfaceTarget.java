/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
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
package org.jacoco.core.test.validation.java8.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This test target builds a constant with a lambda value in an interface.
 */
public interface LambdaInInterfaceTarget {

	public static final Runnable RUN = () -> {
		nop(); // assertFullyCovered()
	};

}
