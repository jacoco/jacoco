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
