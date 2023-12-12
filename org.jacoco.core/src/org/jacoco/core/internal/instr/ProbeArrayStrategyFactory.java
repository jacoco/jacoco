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
package org.jacoco.core.internal.instr;

import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

/**
 * Factory to find a suitable strategy to access the probe array for a given
 * class.
 */
public final class ProbeArrayStrategyFactory {

	private ProbeArrayStrategyFactory() {
	}

	/**
	 * Creates a suitable strategy instance for the class described by the given
	 * reader. Created instance must be used only to process a class or
	 * interface for which it has been created and must be used only once.
	 *
	 * @param classId
	 *            class identifier
	 * @param reader
	 *            reader to get information about the class
	 * @param accessorGenerator
	 *            accessor to the coverage runtime
	 * @return strategy instance
	 */
	public static IProbeArrayStrategy createFor(final long classId,
			final ClassReader reader,
			final IExecutionDataAccessorGenerator accessorGenerator) {

		final String className = reader.getClassName();
		final int version = InstrSupport.getMajorVersion(reader);

		if (isInterfaceOrModule(reader)) {
			final ProbeCounter counter = getProbeCounter(reader);
			if (counter.getCount() == 0) {
				return new NoneProbeArrayStrategy();
			}
			if (version >= Opcodes.V11 && counter.hasMethods()) {
				return new CondyProbeArrayStrategy(className, true, classId,
						accessorGenerator);
			}
			if (version >= Opcodes.V1_8 && counter.hasMethods()) {
				return new InterfaceFieldProbeArrayStrategy(className, classId,
						counter.getCount(), accessorGenerator);
			} else {
				return new LocalProbeArrayStrategy(className, classId,
						counter.getCount(), accessorGenerator);
			}
		} else {
			if (version >= Opcodes.V11) {
				return new CondyProbeArrayStrategy(className, false, classId,
						accessorGenerator);
			}
			return new ClassFieldProbeArrayStrategy(className, classId,
					InstrSupport.needsFrames(version), accessorGenerator);
		}
	}

	private static boolean isInterfaceOrModule(final ClassReader reader) {
		return (reader.getAccess()
				& (Opcodes.ACC_INTERFACE | Opcodes.ACC_MODULE)) != 0;
	}

	private static ProbeCounter getProbeCounter(final ClassReader reader) {
		final ProbeCounter counter = new ProbeCounter();
		reader.accept(new ClassProbesAdapter(counter, false), 0);
		return counter;
	}

}
