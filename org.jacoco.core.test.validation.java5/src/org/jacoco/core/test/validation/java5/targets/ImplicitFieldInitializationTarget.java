/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
