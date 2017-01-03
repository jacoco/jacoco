/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
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

public class BadCycleInterface {

	public interface Base {
		static final Object BASE_CONST = new Child() {
			{
				Stubs.logEvent("baseclinit"); // $line-baseclinit$
			}
		}.childDefaultMethod();

		default void baseDefaultMethod() {
		}
	}

	public interface Child extends Base {
		static final Object CHILD_CONST = new Object() {
			{
				Stubs.logEvent("childclinit"); // $line-childclinit$
			}
		};

		default Object childDefaultMethod() {
			Stubs.logEvent("childdefaultmethod"); // $line-childdefault$
			return null;
		}

		static void childStaticMethod() {
			Stubs.logEvent("childstaticmethod"); // $line-childstatic$
		}
	}

	public static void main(String[] args) {
		Child.childStaticMethod();
	}

}
