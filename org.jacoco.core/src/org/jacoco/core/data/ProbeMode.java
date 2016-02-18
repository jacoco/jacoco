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
package org.jacoco.core.data;

import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.internal.instr.IProbeArray;
import org.jacoco.core.internal.instr.ProbeBooleanArray;
import org.jacoco.core.internal.instr.ProbeDoubleIntArray;
import org.jacoco.core.internal.instr.ProbeIntArray;
import org.jacoco.core.runtime.AgentOptions;

/**
 * Possible values for {@link AgentOptions#PROBE}.
 */
public enum ProbeMode {

	/**
	 * Value for the {@link AgentOptions#PROBE} parameter: This is the long time
	 * probe style of JaCoCo. All that is collected is the existence of
	 * coverage, that is, has an instruction been executed at least once.
	 */
	exists(ProbeBooleanArray.class),

	/**
	 * Value for the {@link AgentOptions#PROBE} parameter: This probe mode
	 * collects a count of the number of times an instruction has been executed.
	 */
	count(ProbeIntArray.class),

	/**
	 * Value for the {@link AgentOptions#PROBE} parameter: This probe mode
	 * collects a count of the number of times an instruction has been executed,
	 * and the number of times an instruction has been executed by a thread
	 * holding no monitors.
	 */
	parallelcount(ProbeDoubleIntArray.class);

	private static final Map<Character, IProbeArray<?>> idMap = makeIdMap();

	private static Map<Character, IProbeArray<?>> makeIdMap() {
		final Map<Character, IProbeArray<?>> idMap = new HashMap<Character, IProbeArray<?>>();

		for (final ProbeMode probeMode : ProbeMode.values()) {
			final Character formatVersionKey = new Character(probeMode
					.getProbeZeroInstance().getFormatVersion());
			if (idMap.containsKey(formatVersionKey)) {
				throw new IllegalStateException("Format Version "
						+ probeMode.getProbeZeroInstance().getFormatVersion()
						+ " is duplicate");
			}
			idMap.put(formatVersionKey, probeMode.getProbeZeroInstance());
		}

		return idMap;
	}

	/**
	 * Returns a Zero length singleton probe instance.
	 * 
	 * @param formatVersion
	 *            the {@code char} formatVersion of the {@code IProbeArray}
	 *            implementation
	 * @return Zero length singleton probe instance associated with the
	 *         formatVersion. If none found, {@code null} is returned.
	 */
	public static IProbeArray<?> getZeroInstance(final char formatVersion) {
		return idMap.get(new Character(formatVersion));
	}

	private final IProbeArray<?> probeInstance;

	private ProbeMode(final Class<? extends IProbeArray<?>> probeType) {
		IProbeArray<?> tmpProbeInstance = null;
		try {
			tmpProbeInstance = probeType.getConstructor(Integer.TYPE)
					.newInstance(new Integer(0));
		} catch (final Exception e) {
			throw new IllegalArgumentException(e);
		}
		probeInstance = tmpProbeInstance;
	}

	/**
	 * Returns a Zero length singleton probe instance.
	 * 
	 * @return a Zero length singleton probe instance.
	 */
	public IProbeArray<?> getProbeZeroInstance() {
		return probeInstance;
	}

}