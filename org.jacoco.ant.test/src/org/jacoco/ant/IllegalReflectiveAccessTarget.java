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

package org.jacoco.ant;

import java.lang.reflect.Constructor;

public class IllegalReflectiveAccessTarget {

	public static void main(String[] args) throws Exception {
		try {
			Class.forName("java.net.UnixDomainSocketAddress");
		} catch (ClassNotFoundException e) {
			// Java < 16
			return;
		}

		final Constructor<?> c = Class.forName("java.lang.Module")
				.getDeclaredConstructors()[0];
		try {
			c.setAccessible(true);
			throw new AssertionError("Exception expected");
		} catch (RuntimeException e) {
			if (!e.getClass().getName()
					.equals("java.lang.reflect.InaccessibleObjectException")) {
				throw new AssertionError(
						"InaccessibleObjectException expected");
			}
		}
	}

}
