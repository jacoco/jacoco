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
		public static void s() {
		}

		public void i() {
		}
	}

	public static class PrivateMethod {
		private static void s() {
		}

		private void i() {
		}
	}

	public static void main(String[] args) {
		/* constructor method references */

		exec(PublicConstructor::new); // assertFullyCovered()
		noexec(PublicConstructor::new); // assertFullyCovered()

		exec(PrivateConstructor::new); // assertFullyCovered()
		noexec(PrivateConstructor::new); // assertMethodReferenceToPrivate()

		exec(String[]::new, 0); // assertFullyCovered()
		noexec(String[]::new); // assertMethodReferenceToArrayConstructor()

		/* static method references */

		exec(PublicMethod::s); // assertFullyCovered()
		noexec(PublicMethod::s); // assertFullyCovered()

		exec(PrivateMethod::s); // assertFullyCovered()
		noexec(PrivateMethod::s); // assertMethodReferenceToPrivate()

		/* unbound method references */

		exec(PublicMethod::i, new PublicMethod()); // assertFullyCovered()
		noexec(PublicMethod::i); // assertFullyCovered()

		exec(PrivateMethod::i, new PrivateMethod()); // assertFullyCovered()
		noexec(PrivateMethod::i); // assertMethodReferenceToPrivate()

		/* bound method references */

		exec(new PublicMethod()::i); // assertFullyCovered()
		noexec(new PublicMethod()::i); // assertFullyCovered()

		exec(new PrivateMethod()::i); // assertFullyCovered()
		noexec(new PrivateMethod()::i); // assertMethodReferenceToPrivate()
	}

}
