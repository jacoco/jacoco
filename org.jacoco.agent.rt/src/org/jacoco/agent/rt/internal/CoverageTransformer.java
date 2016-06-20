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
package org.jacoco.agent.rt.internal;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.internal.instr.Companions;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.RuntimeData;
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

	private final RuntimeData runtimeData;

	private final Companions companions;

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
	 * @param runtimeData
	 *            execution data
	 * @param options
	 *            configuration options for the generator
	 * @param logger
	 *            logger for exceptions during instrumentation
	 */
	public CoverageTransformer(final IRuntime runtime,
			final RuntimeData runtimeData, final AgentOptions options,
			final IExceptionLogger logger) {
		this.instrumenter = new Instrumenter(runtime);
		this.runtimeData = runtimeData;
		this.companions = new Companions(runtimeData);
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
			return instrument(loader, classname, classfileBuffer);
		} catch (final Exception ex) {
			final IllegalClassFormatException wrapper = new IllegalClassFormatException(
					ex.getMessage());
			wrapper.initCause(ex);
			// Report this, as the exception is ignored by the JVM:
			logger.logExeption(wrapper);
			throw wrapper;
		}
	}

	private byte[] instrument(final ClassLoader classLoader,
			final String className, final byte[] buffer) throws IOException {
		try {
			if (classLoader == null) {
				// fallback to old strategies for the bootstrap classes
				return instrumenter.instrument(buffer, className);
			}
			return companions.instrument(classLoader, className, buffer);
		} catch (final RuntimeException e) {
			final IOException ex = new IOException(String.format(
					"Error while instrumenting class %s.", className));
			ex.initCause(e);
			throw ex;
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
			if (!inclNoLocationClasses && !hasSourceLocation(protectionDomain)) {
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
