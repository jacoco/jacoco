/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.instr;

/**
 * A singleton thread-safe lock-free counter designed to count the number of
 * monitors active in any given thread.
 * 
 * @author Omer Azmon
 */
public final class ThreadLocalMonitorCounter {
	private static final Integer ZERO = new Integer(0);
	private static final ThreadLocal<Integer> ACCUM = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return ZERO;
		}
	};

	private ThreadLocalMonitorCounter() {
	}

	private static final void add(final int operand) {
		final int newValue = ACCUM.get().intValue() + operand;
		ACCUM.set(new Integer(newValue < 0 ? 0 : newValue));
	}

	/**
	 * Reset to zero the number of active monitors on the current thread.
	 */
	public static final void reset() {
		ACCUM.set(ZERO);
	}

	/**
	 * Increment the number of active monitors on the current thread.
	 */
	public static final void increment() {
		add(+1);
	}

	/**
	 * Decrement the number of active monitors on the current thread.
	 */
	public static final void decrement() {
		add(-1);
	}

	/**
	 * Returns the No-Lock (no active monitor) status.
	 * 
	 * @return {@code true} if no monitor is active for the current thread;
	 *         Otherwise, {@code false}.
	 */
	public static final boolean isNoLock() {
		final boolean isNoLock = ACCUM.get().intValue() == 0;
		return isNoLock;

	}
}
