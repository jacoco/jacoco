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
package org.jacoco.core.test.validation.java8.targets;

import org.jacoco.core.test.validation.targets.Stubs;

public class BadCycleInterfaceTarget {

	public interface Base {
		static final Object BASE_CONST = new Child() {
			{
				Stubs.logEvent("baseclinit"); // assertBaseClInit()
			}
		}.childDefaultMethod();

		default void baseDefaultMethod() {
		}
	}

	public interface Child extends Base {
		static final Object CHILD_CONST = new Object() {
			{
				Stubs.logEvent("childclinit"); // assertFullyCovered()
			}
		};

		default Object childDefaultMethod() {
			Stubs.logEvent("childdefaultmethod"); // assertChildDefault()
			return null;
		}

		static void childStaticMethod() {
			Stubs.logEvent("childstaticmethod"); // assertFullyCovered()
		}
	}

	public static void main(String[] args) {
		Child.childStaticMethod();
	}

}
