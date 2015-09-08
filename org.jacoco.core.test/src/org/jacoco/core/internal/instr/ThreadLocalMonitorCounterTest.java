/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.junit.AfterClass;
import org.junit.Test;

public class ThreadLocalMonitorCounterTest {

	@AfterClass
	public static void cleanup() {
		ThreadLocalMonitorCounter.reset();
	}

	@Test
	public void constructor() throws Exception {
		Constructor<ThreadLocalMonitorCounter> constructor = ThreadLocalMonitorCounter.class
				.getDeclaredConstructor();
		constructor.setAccessible(true);
		constructor.newInstance();
		// Should get here with no exceptions
	}

	@Test
	public void initial() {
		ThreadLocalMonitorCounter.reset();
		assertTrue(ThreadLocalMonitorCounter.isNoLock());
	}

	@Test
	public void stillInitial() {
		ThreadLocalMonitorCounter.reset();
		ThreadLocalMonitorCounter.decrement();
		assertTrue(ThreadLocalMonitorCounter.isNoLock());
	}

	@Test
	public void increment1() {
		ThreadLocalMonitorCounter.reset();
		ThreadLocalMonitorCounter.increment();
		assertFalse(ThreadLocalMonitorCounter.isNoLock());
	}

	@Test
	public void increment2() {
		ThreadLocalMonitorCounter.reset();
		ThreadLocalMonitorCounter.decrement();
		ThreadLocalMonitorCounter.increment();
		assertFalse(ThreadLocalMonitorCounter.isNoLock());
	}

	@Test
	public void increment_decrement() {
		ThreadLocalMonitorCounter.reset();
		ThreadLocalMonitorCounter.increment();
		ThreadLocalMonitorCounter.decrement();
		assertTrue(ThreadLocalMonitorCounter.isNoLock());
	}

	@Test
	public void increment_decrement2() {
		ThreadLocalMonitorCounter.reset();
		ThreadLocalMonitorCounter.increment();
		ThreadLocalMonitorCounter.increment();
		ThreadLocalMonitorCounter.decrement();
		ThreadLocalMonitorCounter.decrement();
		assertTrue(ThreadLocalMonitorCounter.isNoLock());
	}

	@Test
	public void multiThreadInit() {
		final CyclicBarrier barrier = new CyclicBarrier(3);
		final boolean[] resultArray = new boolean[2];
		new Thread() {
			@Override
			public void run() {
				try {
					barrier.await();
					resultArray[0] = ThreadLocalMonitorCounter.isNoLock();
					barrier.await();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}.start();

		new Thread() {
			@Override
			public void run() {
				try {
					barrier.await();
					resultArray[1] = ThreadLocalMonitorCounter.isNoLock();
					barrier.await();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

		try {
			assertFalse(resultArray[0]);
			assertFalse(resultArray[1]);
			barrier.await();
			barrier.await();
			assertTrue(resultArray[0]);
			assertTrue(resultArray[1]);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void multiThreadIncrement() {
		final CyclicBarrier barrier = new CyclicBarrier(3);
		final boolean[] resultArray = new boolean[2];
		new Thread() {
			@Override
			public void run() {
				try {
					barrier.await();
					ThreadLocalMonitorCounter.increment();
					ThreadLocalMonitorCounter.increment();
					ThreadLocalMonitorCounter.increment();
					resultArray[0] = ThreadLocalMonitorCounter.isNoLock();
					barrier.await();
					barrier.await();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}.start();

		new Thread() {
			@Override
			public void run() {
				try {
					barrier.await();
					barrier.await();
					resultArray[1] = ThreadLocalMonitorCounter.isNoLock();
					barrier.await();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

		try {
			assertFalse(resultArray[0]);
			assertFalse(resultArray[1]);
			barrier.await();
			barrier.await();
			barrier.await();
			assertFalse(resultArray[0]);
			assertTrue(resultArray[1]);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			e.printStackTrace();
		}
	}

}
