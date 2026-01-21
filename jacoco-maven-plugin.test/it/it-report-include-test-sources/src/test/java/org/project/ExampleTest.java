/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
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
package org.project;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ExampleTest {

	@Test
	public void test() {
		Example example = new Example();
		assertEquals("Hello from main", example.getMessage());
		TestHelper helper = new TestHelper();
		assertEquals("Hello from test", helper.getTestMessage());
	}

}
