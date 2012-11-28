/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    Martin Hare Robertson - filters
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation.targets;

/**
 * This test target is a class with a implicit default constructor and another
 * method which isn't blank.
 */
public class Target06 { // $line-classdef$

	public static void main(String[] args) {
		System.setProperty("noop", "noop");
	}

}
