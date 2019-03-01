/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java5.targets;

import org.jacoco.core.test.validation.targets.Stubs;

public class BadCycleClassTarget {

	public static class Base {
		static final Child b = new Child();

		static {
			b.someMethod();
		}
	}

	public static class Child extends Base {

		static {
			Stubs.logEvent("childclinit"); // assertFullyCovered()
		}

		public Child() {
			Stubs.logEvent("childinit"); // assertFullyCovered()
		}

		void someMethod() {
			Stubs.logEvent("childsomeMethod"); // assertFullyCovered()
		}

	}

	public static void main(String[] args) {
		new Child();
	}

}
