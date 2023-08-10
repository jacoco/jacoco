/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java16;

import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java16.targets.RecordsTarget;

/**
 * Test of code coverage for records.
 */
public class RecordsTest extends ValidationTestBase {

	public RecordsTest() {
		super(RecordsTarget.class);
	}

}
