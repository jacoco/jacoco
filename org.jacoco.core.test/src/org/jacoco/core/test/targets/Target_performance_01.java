package org.jacoco.core.test.targets;

/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Marc R. Hoffmann - initial API and implementation
 * 
 * $Id: $
 *******************************************************************************/

/**
 * 1,398,101 plain method calls.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class Target_performance_01 implements Runnable {

	// 4 ^ 0 = 1 times
	public void run() {
		m1();
		m1();
		m1();
		m1();
	}

	// 4 ^ 1 = 4 times
	private void m1() {
		m2();
		m2();
		m2();
		m2();
	}

	// 4 ^ 2 == 16 times
	private void m2() {
		m3();
		m3();
		m3();
		m3();
	}

	// 4 ^ 3 == 64 times
	private void m3() {
		m4();
		m4();
		m4();
		m4();
	}

	// 4 ^ 4 == 256 times
	private void m4() {
		m5();
		m5();
		m5();
		m5();
	}

	// 4 ^ 5 == 1,024 times
	private void m5() {
		m6();
		m6();
		m6();
		m6();
	}

	// 4 ^ 6 == 4,096 times
	private void m6() {
		m7();
		m7();
		m7();
		m7();
	}

	// 4 ^ 7 == 16,384 times
	private void m7() {
		m8();
		m8();
		m8();
		m8();
	}

	// 4 ^ 8 == 65,536 times
	private void m8() {
		m9();
		m9();
		m9();
		m9();
	}

	// 4 ^ 9 == 262,144 times
	private void m9() {
		m10();
		m10();
		m10();
		m10();
	}

	// 4 ^ 10 == 1,048,576 times
	private void m10() {
	}

	@Override
	public String toString() {
		return "plain method calls";
	}

}
