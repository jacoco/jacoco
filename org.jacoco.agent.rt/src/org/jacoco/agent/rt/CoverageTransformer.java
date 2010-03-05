/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.agent.rt;

import static java.lang.String.format;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.jacoco.core.instr.CRC64;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.WildcardMatcher;

/**
 * Class file transformer to instrument classes for code coverage analysis.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CoverageTransformer implements ClassFileTransformer {

	private final Instrumenter instrumenter;

	private final WildcardMatcher includes;

	private final WildcardMatcher excludes;

	private final WildcardMatcher exclClassloader;

	public CoverageTransformer(IRuntime runtime, AgentOptions options) {
		this.instrumenter = new Instrumenter(runtime);
		// Class names will be reported in VM notation:
		includes = new WildcardMatcher(options.getIncludes().replace('.', '/'));
		excludes = new WildcardMatcher(options.getExcludes().replace('.', '/'));
		exclClassloader = new WildcardMatcher(options.getExclClassloader());
	}

	public byte[] transform(ClassLoader loader, String classname,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {

		if (!filter(loader, classname)) {
			return null;
		}

		try {
			return instrumenter.instrument(classfileBuffer);
		} catch (Throwable t) {
			final Long id = Long.valueOf(CRC64.checksum(classfileBuffer));
			final String msg = "Error while instrumenting class %s (id=0x%x).";
			final IllegalClassFormatException ex = new IllegalClassFormatException(
					format(msg, classname, id));
			ex.initCause(t);
			// Force some output, as the exception is ignored by the JVM:
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * Checks whether this class should be instrumented.
	 * 
	 * @param loader
	 *            loader for the class
	 * @return <code>true</code> if the class should be instrumented
	 */
	protected boolean filter(ClassLoader loader, String classname) {
		// Don't instrument classes of the bootstrap loader:
		return loader != null &&

		!exclClassloader.matches(loader.getClass().getName()) &&

		includes.matches(classname) &&

		!excludes.matches(classname);
	}
}
