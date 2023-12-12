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
package org.jacoco.core.test.validation.targets;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection of stub methods that are called from the coverage targets. *
 */
public class Stubs {

	/**
	 * Exception stub.
	 */
	public static class StubException extends RuntimeException {

		static final long serialVersionUID = 0L;

	}

	/**
	 * Superclass stub.
	 */
	public static class SuperClass {

		public SuperClass(boolean arg) {
		}

	}

	/**
	 * Enum stub.
	 */
	public static enum Enum {
		A, B, C
	}

	/**
	 * Dummy method.
	 */
	public static void nop() {
	}

	/**
	 * Dummy method.
	 */
	public static void nop(int i) {
	}

	/**
	 * Dummy method.
	 */
	public static void nop(boolean b) {
	}

	/**
	 * Dummy method.
	 */
	public static void nop(Object o) {
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
	 * @return always <code>1</code>
	 */
	public static int i1() {
		return 1;
	}

	/**
	 * @return always <code>3</code>
	 */
	public static int i2() {
		return 2;
	}

	/**
	 * @return always <code>3</code>
	 */
	public static int i3() {
		return 3;
	}

	/**
	 * @return always <code>A</code>
	 */
	public static Enum enumA() {
		return Enum.A;
	}

	/**
	 * Always throws a {@link RuntimeException}.
	 *
	 * @throws StubException
	 *             always thrown
	 */
	public static void ex() throws StubException {
		throw new StubException();
	}

	/**
	 * Throws a {@link RuntimeException} if given argument is <code>true</code>.
	 */
	public static void ex(boolean t) {
		if (t) {
			throw new StubException();
		}
	}

	/**
	 * Directly executes the given runnable.
	 */
	public static void exec(Runnable task) {
		task.run();
	}

	/**
	 * Never executes the given runnable.
	 */
	public static void noexec(Runnable task) {
	}

	/**
	 * List of logged events. Using a static member here works as this class is
	 * loaded in a new class loader for every test case.
	 */
	private static List<String> events = new ArrayList<String>();

	/**
	 * Records a event with the given id for later verification.
	 */
	public static void logEvent(String id) {
		events.add(id);
	}

	/**
	 * Returns a list of all recorded events in the sequence of recording.
	 */
	public static List<String> getLogEvents() {
		return events;
	}

}
