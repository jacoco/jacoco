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
package org.jacoco.core.test.perf;

/**
 * Interface for a performance scenario.
 */
public interface IPerfScenario {

	/**
	 * Runs the performance scenario and reports the result to the given
	 * interface.
	 *
	 * @param output
	 */
	void run(IPerfOutput output) throws Exception;

}
