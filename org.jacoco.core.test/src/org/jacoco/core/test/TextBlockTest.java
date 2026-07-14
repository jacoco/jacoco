/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TextBlockTest {

	@Test
	public void test() {
		assertEquals("1\n", TextBlock.lines("1"));
		assertEquals("1\n2\n", TextBlock.lines( //
				"1", //
				"2"));
	}

}
