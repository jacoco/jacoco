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
package org.jacoco.core.test.filter.targets;

import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This test target is a constructors.
 */
public class Constructor {

	Constructor() { // $line-packageLocal$
	}

	private Constructor(Object arg) { // $line-arg$
	}

	private static class Super extends Constructor {
		private Super() {
			super(null); // $line-super$
		}
	}

	private class Inner {
		private Inner() { // $line-inner$
		}
	}

	private static class InnerStatic {
		private Object field = this;

		private InnerStatic() { // $line-innerStatic$
		}
	}

	public static class PublicDefault { // $line-publicDefault$
	}

	static class PackageLocalDefault { // $line-packageLocalDefault$
	}

	private static class PrivateDefault { // $line-privateDefault$
	}

	private static class PrivateNonEmptyNoArg {
		private PrivateNonEmptyNoArg() {
			nop(); // $line-privateNonEmptyNoArg$
		}
	}

	private static class PrivateEmptyNoArg {
		private PrivateEmptyNoArg() { // $line-privateEmptyNoArg$
		} // $line-return$
	}

	public static void main(String[] args) {
		new Super();
		new Constructor().new Inner();
		new InnerStatic();
		new PublicDefault();
		new PackageLocalDefault();
		new PrivateDefault();
		new PrivateNonEmptyNoArg();
		new PrivateEmptyNoArg();
	}

}
