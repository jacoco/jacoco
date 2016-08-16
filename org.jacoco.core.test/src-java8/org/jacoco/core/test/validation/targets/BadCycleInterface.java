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

public class BadCycleInterface {

	public interface Base {
		static final Object BASE_CONST = new Child() {
			{
				Stubs.nop("base clinit"); // $line-1$
			}
		}.childDefaultMethod();

		default void baseDefaultMethod() {
		}
	}

	public interface Child extends Base {
		static final Object CHILD_CONST = new Object() {
			{
				Stubs.nop("child clinit"); // $line-3$
			}
		};

		default Object childDefaultMethod() {
			Stubs.nop("child default method"); // $line-2$
			return null;
		}

		static void childStaticMethod() {
			Stubs.nop("child static method"); // $line-4$
		}
	}

	public static void main(String[] args) {
		Child.childStaticMethod();
	}

}
