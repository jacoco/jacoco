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

public class AggregateExampleTest {

	@Test
	public void test() {
		AggregateExample example = new AggregateExample();
		assertEquals("Aggregate value", example.getValue());
		AggregateTestUtil util = new AggregateTestUtil();
		assertEquals("Test utility", util.getUtilValue());
	}

}
