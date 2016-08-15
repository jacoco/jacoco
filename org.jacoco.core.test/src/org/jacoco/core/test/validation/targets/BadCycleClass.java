/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.targets;

public class BadCycleClass {

	public static class Base {
		static final Child b = new Child();

		static {
			b.someMethod();
		}
	}

	public static class Child extends Base {

		static {
			Stubs.nop("child clinit"); // $line-3$
		}

		public Child() {
			Stubs.nop("child init"); // $line-1$
		}

		void someMethod() {
			Stubs.nop("child someMethod"); // $line-2$
		}

	}

	public static void main(String[] args) {
		new Child();
	}

}
