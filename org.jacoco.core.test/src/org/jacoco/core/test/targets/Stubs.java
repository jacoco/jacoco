/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.test.targets;

/**
 * Collection of stub methods that are called from the coverage targets. *
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class Stubs {

	/**
	 * Does nothing.
	 */
	public static void nop() {
	}

	/**
	 * @return always <code>true</code>
	 */
	public static boolean t() {
		return true;
	}

	/**
	 * @return always <code>false</code>
	 */
	public static boolean f() {
		return false;
	}

	/**
	 * Marker method m1.
	 */
	public static void m1() {
	}

	/**
	 * Marker method m1.
	 */
	public static boolean m1(boolean value) {
		return value;
	}

	/**
	 * Marker method m1.
	 */
	public static void m2() {
	}

	/**
	 * Marker method m1.
	 */
	public static boolean m2(boolean value) {
		return value;
	}

	/**
	 * Marker method m1.
	 */
	public static void m3() {
	}

	/**
	 * Marker method m1.
	 */
	public static boolean m3(boolean value) {
		return value;
	}

	public static class Base {

		public Base() {
		}

		public Base(boolean b) {
		}

	}

}
