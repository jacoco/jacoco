/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation.targets;

/**
 * This test target has instance members with implicit initializers.
 */
public class Target08 { // $line-classdef$

	Object field1; // $line-field1$

	Object field2 = this; // $line-field2$

	int field3; // $line-field3$

	int field4 = 2000; // $line-field4$

	public static void main(String[] args) {
		new Target08();
	}

}
