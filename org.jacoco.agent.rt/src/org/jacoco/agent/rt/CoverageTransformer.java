/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt;

import static java.lang.String.format;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.jacoco.core.runtime.WildcardMatcher;

/**
 * Class file transformer to instrument classes for code coverage analysis.
 */
public class CoverageTransformer implements ClassFileTransformer {

	private static final String AGENT_PREFIX;

	static {
		final String name = CoverageTransformer.class.getName();
		AGENT_PREFIX = toVMName(name.substring(0, name.lastIndexOf('.')));
	}

	private final IExceptionLogger logger;

	private final Instrumenter instrumenter;

	private final WildcardMatcher includes;

	private final WildcardMatcher excludes;

	private final WildcardMatcher exclClassloader;

	/**
	 * New transformer with the given delegates.
	 * 
	 * @param generator
	 *            generator for runtime specific access code
	 * @param options
	 *            configuration options for the generator
	 * @param logger
	 *            logger for exceptions during instrumentation
	 */
	public CoverageTransformer(final IExecutionDataAccessorGenerator generator,
			final AgentOptions options, final IExceptionLogger logger) {
		this.instrumenter = new Instrumenter(generator);
		this.logger = logger;
		// Class names will be reported in VM notation:
		includes = new WildcardMatcher(
				toWildcard(toVMName(options.getIncludes())));
		excludes = new WildcardMatcher(
				toWildcard(toVMName(options.getExcludes())));
		exclClassloader = new WildcardMatcher(
				toWildcard(options.getExclClassloader()));
	}

	public byte[] transform(final ClassLoader loader, final String classname,
			final Class<?> classBeingRedefined,
			final ProtectionDomain protectionDomain,
			final byte[] classfileBuffer) throws IllegalClassFormatException {

		if (!filter(loader, classname)) {
			return null;
		}

		try {
			return instrumenter.instrument(classfileBuffer);
		} catch (final Throwable t) {
			final String msg = "Error while instrumenting class %s.";
			final IllegalClassFormatException ex = new IllegalClassFormatException(
					format(msg, classname));
			// Report this, as the exception is ignored by the JVM:
			logger.logExeption(ex);
			throw (IllegalClassFormatException) ex.initCause(t);
		}
	}

	/**
	 * Checks whether this class should be instrumented.
	 * 
	 * @param loader
	 *            loader for the class
	 * @param classname
	 *            VM name of the class to check
	 * @return <code>true</code> if the class should be instrumented
	 */
	protected boolean filter(final ClassLoader loader, final String classname) {
		// Don't instrument classes of the bootstrap loader:
		return loader != null &&

		!classname.startsWith(AGENT_PREFIX) &&

		!exclClassloader.matches(loader.getClass().getName()) &&

		includes.matches(classname) &&

		!excludes.matches(classname);
	}

	private String toWildcard(final String src) {
		if (src.indexOf('|') != -1) {
			final IllegalArgumentException ex = new IllegalArgumentException(
					"Usage of '|' as a list separator for JaCoCo agent options is deprecated and will not work in future versions - use ':' instead.");
			logger.logExeption(ex);
			return src.replace('|', ':');
		}
		return src;
	}

	private static String toVMName(final String srcName) {
		return srcName.replace('.', '/');
	}

}
