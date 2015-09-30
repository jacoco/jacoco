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
package org.jacoco.core.instr;

import org.jacoco.core.data.ProbeMode;
import org.jacoco.core.internal.instr.ProbeArrayService;

/**
 * An instrumentation configuration access point to select different types of
 * data collection probes. By default we continue to use <code>boolean[]</code>,
 * but allow more advanced features to activated by those that choose so.
 * 
 * @author Omer Azmon
 */
public class InstrumentationConfig {

	/**
	 * Configure the instrumentation probing mode.
	 * 
	 * @param requestedProbeMode
	 *            the probe mode desired.
	 */
	public static void configure(final ProbeMode requestedProbeMode) {
		ProbeArrayService.configure(requestedProbeMode);
	}

	/**
	 * Reset to un-configured state
	 */
	public static void reset() {
		ProbeArrayService.reset();
	}

	/**
	 * Returns the current instrumentation probing mode.
	 * 
	 * @return the current instrumentation probing mode.
	 */
	public static ProbeMode getProbeMode() {
		return ProbeArrayService.getProbeMode();
	}
}
