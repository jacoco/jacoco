/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java5.targets;

/**
 * This test target has instance members with implicit initializers.
 */
public class ImplicitFieldInitializationTarget { // assertFullyCovered()

	Object field1; // assertEmpty()

	Object field2 = this; // assertFullyCovered()

	int field3; // assertEmpty()

	int field4 = 2000; // assertFullyCovered()

	public static void main(String[] args) {
		new ImplicitFieldInitializationTarget();
	}

}
