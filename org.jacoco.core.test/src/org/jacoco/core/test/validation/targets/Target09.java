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
 * This test target has instance members with initialization in two
 * constructors.
 */
public class Target09 {

	Object field1 = null; // $line-field1$

	int field2 = 123; // $line-field2$

	public Target09() {
	} // $line-constr1$

	public Target09(String arg) {
	} // $line-constr2$

	public static void main(String[] args) {
		new Target09();
	}

}
