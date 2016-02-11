/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HeaderInfoTest {

	@Test
	public void HeaderInfo() {
		HeaderInfo instance = new HeaderInfo('A');
		assertEquals('A', instance.getFormatVersion());
		assertEquals("Header[0x41]", instance.toString());
	}

	@Test(expected = NullPointerException.class)
	public void compareToNull() {
		new HeaderInfo('A').compareTo(null);
	}

	@Test
	public void compareToSelf() {
		HeaderInfo instance = new HeaderInfo('A');
		assertEquals(0, instance.compareTo(instance));
	}

	@Test
	public void compareToAnotherEquals() {
		HeaderInfo instance1 = new HeaderInfo('A');
		HeaderInfo instance2 = new HeaderInfo('A');
		assertEquals(0, instance1.compareTo(instance2));
		assertEquals(0, instance2.compareTo(instance1));
	}

	@Test
	public void compareToAnotherNotEquals() {
		HeaderInfo instance1 = new HeaderInfo('A');
		HeaderInfo instance2 = new HeaderInfo('B');
		assertEquals(-1, instance1.compareTo(instance2));
		assertEquals(+1, instance2.compareTo(instance1));
	}
}
