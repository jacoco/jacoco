/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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
package org.jacoco.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

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

	private final WildcardMatcher exclClassloader;

	public CoverageTransformer(IRuntime runtime, AgentOptions options) {
		this.instrumenter = new Instrumenter(runtime);
		exclClassloader = new WildcardMatcher(options.getExclClassloader());
	}

	public byte[] transform(ClassLoader loader, String classname,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {

		if (!filter(loader)) {
			return null;
		}

		try {
			return instrumenter.instrument(classfileBuffer);
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	/**
	 * Checks whether this class should be instrumented.
	 * 
	 * @param loader
	 *            loader for the class
	 * @return <code>true</code> if the class should be instrumented
	 */
	protected boolean filter(ClassLoader loader) {
		// Don't instrument classes of the bootstrap loader:
		if (loader == null) {
			return false;
		}
		if (exclClassloader.matches(loader.getClass().getName())) {
			return false;
		}
		return true;
	}

}
