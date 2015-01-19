/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	private final boolean includeBootstrapClasses;

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
		includeBootstrapClasses = options.getInclBootstrapClasses();
	}

	public byte[] transform(final ClassLoader loader, final String classname,
			final Class<?> classBeingRedefined,
			final ProtectionDomain protectionDomain,
			final byte[] classfileBuffer) throws IllegalClassFormatException {

		// We do not support class retransformation:
		if (classBeingRedefined != null) {
			return null;
		}

		// We exclude dynamically created non-bootstrap classes.
		if (loader != null && !hasSourceLocation(protectionDomain)) {
			return null;
		}

		if (!filter(loader, classname)) {
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
	 * Checks whether this protection domain is associated with a source
	 * location.
	 * 
	 * @param protectionDomain
	 *            protection domain to check (or <code>null</code>)
	 * @return <code>true</code> if a source location is defined
	 */
	boolean hasSourceLocation(final ProtectionDomain protectionDomain) {
		if (protectionDomain == null) {
			return false;
		}
		final CodeSource codeSource = protectionDomain.getCodeSource();
		if (codeSource == null) {
			return false;
		}
		return codeSource.getLocation() != null;
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
	boolean filter(final ClassLoader loader, final String classname) {
		if (loader == null) {
			if (!includeBootstrapClasses) {
				return false;
			}
		} else {
			if (exclClassloader.matches(loader.getClass().getName())) {
				return false;
			}
		}

		return !classname.startsWith(AGENT_PREFIX) &&

		includes.matches(classname) &&

		!excludes.matches(classname);
	}

	private static String toVMName(final String srcName) {
		return srcName.replace('.', '/');
	}

}
