/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.perf;

/**
 * Base class for execution time test scenarios.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public abstract class TimedScenario implements IPerfScenario {

	private static final int RUNS = 10;

	private final String description;

	protected TimedScenario(final String description) {
		this.description = description;
	}

	public void run(final IPerfOutput output) throws Exception {
		final long time = getMinimumTime(getInstrumentedRunnable());
		final Runnable refRunnable = getReferenceRunnable();
		final long reftime;
		if (refRunnable == null) {
			reftime = IPerfOutput.NO_REFERENCE;
		} else {
			reftime = getMinimumTime(refRunnable);
		}
		output.writeTimeResult(description, time, reftime);
	}

	/**
	 * Runs the given subject several times and returns the minimum execution
	 * time.
	 * 
	 * @param subject
	 * @return minimum execution time in nano seconds
	 */
	private long getMinimumTime(final Runnable subject) {
		long min = Long.MAX_VALUE;
		for (int i = 0; i < RUNS; i++) {
			final long t = getTime(subject);
			min = Math.min(min, t);
		}
		return min;
	}

	private long getTime(final Runnable subject) {
		long start = System.nanoTime();
		subject.run();
		return System.nanoTime() - start;
	}

	protected abstract Runnable getInstrumentedRunnable() throws Exception;

	protected Runnable getReferenceRunnable() throws Exception {
		return null;
	}

}
