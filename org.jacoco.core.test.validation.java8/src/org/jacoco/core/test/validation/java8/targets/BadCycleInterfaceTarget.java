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
