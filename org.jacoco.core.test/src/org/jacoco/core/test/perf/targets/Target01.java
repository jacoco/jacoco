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
package org.jacoco.core.test.perf.targets;

import java.util.concurrent.Callable;

/**
 * Plain method calls.
 */
public class Target01 implements Callable<Void> {

	@SuppressWarnings("unused")
	private int c;

	// 4 ^ 0 = 1 times
	public Void call() throws Exception {
		m1();
		m1();
		m1();
		m1();
		c++; // some side effect, otherwise the JIT will remove the method
		return null;
	}

	// 4 ^ 1 = 4 times
	public void m1() {
		m2();
		m2();
		m2();
		m2();
		c++; // some side effect, otherwise the JIT will remove the method
	}

	// 4 ^ 2 == 16 times
	public void m2() {
		m3();
		m3();
		m3();
		m3();
		c++; // some side effect, otherwise the JIT will remove the method
	}

	// 4 ^ 3 == 64 times
	public void m3() {
		m4();
		m4();
		m4();
		m4();
		c++; // some side effect, otherwise the JIT will remove the method
	}

	// 4 ^ 4 == 256 times
	public void m4() {
		m5();
		m5();
		m5();
		m5();
		c++; // some side effect, otherwise the JIT will remove the method
	}

	// 4 ^ 5 == 1,024 times
	public void m5() {
		m6();
		m6();
		m6();
		m6();
		c++; // some side effect, otherwise the JIT will remove the method
	}

	// 4 ^ 6 == 4,096 times
	public void m6() {
		m7();
		m7();
		m7();
		m7();
		c++; // some side effect, otherwise the JIT will remove the method
	}

	// 4 ^ 7 == 16,384 times
	public void m7() {
		m8();
		m8();
		m8();
		m8();
		c++; // some side effect, otherwise the JIT will remove the method
	}

	// 4 ^ 8 == 65,536 times
	public void m8() {
		m9();
		m9();
		m9();
		m9();
		c++; // some side effect, otherwise the JIT will remove the method
	}

	// 4 ^ 9 == 262,144 times
	public void m9() {
		m10();
		m10();
		m10();
		m10();
		c++; // some side effect, otherwise the JIT will remove the method
	}

	// 4 ^ 10 == 1,048,576 times
	public void m10() {
		m11();
		m11();
		m11();
		m11();
		c++; // some side effect, otherwise the JIT will remove the method
	}

	// 4 ^ 11 == 4,194,304 times
	public void m11() {
		c++; // some side effect, otherwise the JIT will remove the method
	}

}
