/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassReader;

/**
 * Factory to find a suitable strategy to access the probe array for a given
 * class.
 */
public final class ProbeArrayStrategyFactory {

	private ProbeArrayStrategyFactory() {
	}

	/**
	 * Creates a suitable strategy instance for the class described by the given
	 * reader.
	 * 
	 * @param reader
	 *            reader to get information about the class
	 * @param accessorGenerator
	 *            accessor to the coverage runtime
	 * @return strategy instance
	 */
	public static IProbeArrayStrategy createFor(final ClassReader reader,
			final IExecutionDataAccessorGenerator accessorGenerator) {

		final String className = reader.getClassName();
		final long classId = CRC64.checksum(reader.b);

		final ProbeCounter counter = getProbeCounter(reader);
		if (counter.getCount() == 0) {
			return new NoneProbeArrayStrategy();
		} else {
			return new LocalProbeArrayStrategy(className, classId,
					counter.getCount(), accessorGenerator);
		}
	}

	private static ProbeCounter getProbeCounter(final ClassReader reader) {
		final ProbeCounter counter = new ProbeCounter();
		reader.accept(new ClassProbesAdapter(counter, false), 0);
		return counter;
	}

}
