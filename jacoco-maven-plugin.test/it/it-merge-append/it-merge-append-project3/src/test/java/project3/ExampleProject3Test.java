/*******************************************************************************
 * Copyright (c) 2009, 2022 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Mads Mohr Christensen - implementation of MergeMojo
 *
 *******************************************************************************/
package project3;

import project1.ExampleProject1;

import org.junit.Test;

public class ExampleProject3Test {

	@Test
	public void test() {
		new ExampleProject3().sayHello();
	}

	@Test
	public void partiallyTestProject1(){
		new ExampleProject1().sayHowdy();
	}

}
