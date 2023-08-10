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
package org.jacoco.agent.rt.internal;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;
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

	private final Instrumenter instrumenter;

	private final IExceptionLogger logger;

	private final WildcardMatcher includes;

	private final WildcardMatcher excludes;

	private final WildcardMatcher exclClassloader;

	private final ClassFileDumper classFileDumper;

	private final boolean inclBootstrapClasses;

	private final boolean inclNoLocationClasses;

	/**
	 * New transformer with the given delegates.
	 *
	 * @param runtime
	 *            coverage runtime
	 * @param options
	 *            configuration options for the generator
	 * @param logger
	 *            logger for exceptions during instrumentation
	 */
	public CoverageTransformer(final IRuntime runtime,
			final AgentOptions options, final IExceptionLogger logger) {
		this.instrumenter = new Instrumenter(runtime);
		this.logger = logger;
		// Class names will be reported in VM notation:
		includes = new WildcardMatcher(toVMName(options.getIncludes()));
		excludes = new WildcardMatcher(toVMName(options.getExcludes()));
		exclClassloader = new WildcardMatcher(options.getExclClassloader());
		classFileDumper = new ClassFileDumper(options.getClassDumpDir());
		inclBootstrapClasses = options.getInclBootstrapClasses();
		inclNoLocationClasses = options.getInclNoLocationClasses();
	}

	public byte[] transform(final ClassLoader loader, final String classname,
			final Class<?> classBeingRedefined,
			final ProtectionDomain protectionDomain,
			final byte[] classfileBuffer) throws IllegalClassFormatException {

		// We do not support class retransformation:
		if (classBeingRedefined != null) {
			return null;
		}

		if (!filter(loader, classname, protectionDomain)) {
			return null;
		}

		try {
			classFileDumper.dump(classname, classfileBuffer);
			return instrumenter.instrument(classfileBuffer, classname);
		} catch (final Exception ex) {
			final IllegalClassFormatException wrapper = new IllegalClassFormatException(
					ex.getMessage());
			wrapper.initCause(ex);
			// Report this, as the exception is ignored by the JVM:
			logger.logExeption(wrapper);
			throw wrapper;
		}
	}

	/**
	 * Checks whether this class should be instrumented.
	 *
	 * @param loader
	 *            loader for the class
	 * @param classname
	 *            VM name of the class to check
	 * @param protectionDomain
	 *            protection domain for the class
	 * @return <code>true</code> if the class should be instrumented
	 */
	boolean filter(final ClassLoader loader, final String classname,
			final ProtectionDomain protectionDomain) {
		if (loader == null) {
			if (!inclBootstrapClasses) {
				return false;
			}
		} else {
			if (!inclNoLocationClasses
					&& !hasSourceLocation(protectionDomain)) {
				return false;
			}
			if (exclClassloader.matches(loader.getClass().getName())) {
				return false;
			}
		}

		return !classname.startsWith(AGENT_PREFIX) &&

				includes.matches(classname) &&

				!excludes.matches(classname);
	}

	/**
	 * Checks whether this protection domain is associated with a source
	 * location.
	 *
	 * @param protectionDomain
	 *            protection domain to check (or <code>null</code>)
	 * @return <code>true</code> if a source location is defined
	 */
	private boolean hasSourceLocation(final ProtectionDomain protectionDomain) {
		if (protectionDomain == null) {
			return false;
		}
		final CodeSource codeSource = protectionDomain.getCodeSource();
		if (codeSource == null) {
			return false;
		}
		return codeSource.getLocation() != null;
	}

	private static String toVMName(final String srcName) {
		return srcName.replace('.', '/');
	}

}
