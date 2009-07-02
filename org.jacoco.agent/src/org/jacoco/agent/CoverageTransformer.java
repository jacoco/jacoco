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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;

/**
 * Class file transformer to instrument classes for code coverage analysis.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class CoverageTransformer implements ClassFileTransformer {

	/**
	 * Internal class loaders used by JVM implementations for dynamically
	 * created classes. We must not instrument classes loaded by these loaders.
	 */
	private static final Set<String> EVIL_CLASSLOADERS = new HashSet<String>(
			Arrays.asList("sun.reflect.DelegatingClassLoader"));

	private final Instrumenter instrumenter;

	public CoverageTransformer(IRuntime runtime) {
		this.instrumenter = new Instrumenter(runtime);
	}

	public byte[] transform(ClassLoader loader, String classname,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {

		if (loader == null) {
			// don't instrument classes of the bootstrap loader
			return null;
		}

		if (EVIL_CLASSLOADERS.contains(loader.getClass().getName())) {
			return null;
		}

		try {
			return instrumenter.instrument(classfileBuffer);
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}
}
