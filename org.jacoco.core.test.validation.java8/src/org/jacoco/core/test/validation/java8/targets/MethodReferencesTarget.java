/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java8.targets;

import static org.jacoco.core.test.validation.targets.Stubs.exec;
import static org.jacoco.core.test.validation.targets.Stubs.noexec;

/**
 * This test target contains different method references.
 */
public class MethodReferencesTarget {

	public static class PublicConstructor {
	}

	private static class PrivateConstructor {
	}

	static class PublicMethod {
		public static void m() {
		}
	}

	static class PrivateMethod {
		private static void m() {
		}
	}

	public static void main(String[] args) {
		exec(PublicConstructor::new); // assertFullyCovered()
		noexec(PublicConstructor::new); // assertFullyCovered()

		exec(PrivateConstructor::new); // assertFullyCovered()
		noexec(PrivateConstructor::new); // assertMethodReferenceToPrivate()

		exec(PublicMethod::m); // assertFullyCovered()
		noexec(PublicMethod::m); // assertFullyCovered()

		exec(PrivateMethod::m); // assertFullyCovered()
		noexec(PrivateMethod::m); // assertMethodReferenceToPrivate()

		exec(String[]::new); // assertFullyCovered()
		noexec(String[]::new); // assertMethodReferenceToArrayConstructor()
	}

}
