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

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This test target calls different constructors.
 */
public class ConstructorsTarget {

	/* not filtered because not private: */
	ConstructorsTarget() { // assertFullyCovered()
	}

	/* not filtered because has argument: */
	private ConstructorsTarget(Object arg) { // assertFullyCovered()
	}

	private static class Super extends ConstructorsTarget {
		private Super() {
			/*
			 * not filtered because not empty - prepares arguments for super
			 * constructor:
			 */
			super(null); // assertFullyCovered()
		}
	}

	private class Inner {
		/*
		 * not filtered because contains initialization of a field to hold
		 * reference to an instance of outer class that is passed as an
		 * argument:
		 */
		private Inner() { // assertFullyCovered()
		}
	}

	private static class InnerStatic {
		@SuppressWarnings("unused")
		private final Object field = this;

		/*
		 * not filtered because not empty - contains initialization of a field:
		 */
		private InnerStatic() { // assertFullyCovered()
		}
	}

	/*
	 * not filtered because default constructor for not private inner classes is
	 * not private:
	 */
	public static class PublicDefault { // assertFullyCovered()
	}

	static class PackageLocalDefault { // assertFullyCovered()
	}

	private static class PrivateDefault { // assertEmpty()
	}

	private static class PrivateNonEmptyNoArg {
		private PrivateNonEmptyNoArg() {
			nop(); // assertFullyCovered()
		}
	}

	private static class PrivateEmptyNoArg {
		private PrivateEmptyNoArg() { // assertEmpty()
		} // assertEmpty()
	}

	public static void main(String[] args) {
		new Super();
		new ConstructorsTarget().new Inner();
		new InnerStatic();
		new PublicDefault();
		new PackageLocalDefault();
		new PrivateDefault();
		new PrivateNonEmptyNoArg();
		new PrivateEmptyNoArg();
	}

}
