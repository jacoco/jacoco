/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Keeping - initial implementation
 *
 *******************************************************************************/
package org.jacoco.cli;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.jacoco.core.runtime.WildcardMatcher;
import org.objectweb.asm.ClassReader;

class FilteredInstrumenter extends Instrumenter {

	private final WildcardMatcher include;
	private final WildcardMatcher exclude;
	private int adjustment = 0;

	public FilteredInstrumenter(final IExecutionDataAccessorGenerator runtime,
			final WildcardMatcher include, final WildcardMatcher exclude) {
		super(runtime);
		this.include = include;
		this.exclude = exclude;
	}

	public int getAdjustment() {
		return adjustment;
	}

	@Override
	public byte[] instrument(final ClassReader reader) {
		if (!includeClass(reader.getClassName())) {
			--adjustment;
			return reader.b;
		}
		return super.instrument(reader);
	}

	private boolean includeClass(final String internalClassName) {
		final String className = internalClassName.replace('/', '.');
		return include.matches(className) && !exclude.matches(className);
	}

}
