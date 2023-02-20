/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test;

import java.io.IOException;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.runtime.SystemPropertiesRuntime;

/**
 * Class loader which loads classes from another class loader and instruments
 * them.
 */
public final class InstrumentingLoader extends ClassLoader {

	private final IRuntime runtime;
	private final String scope;
	private final ClassLoader delegate;

	private final RuntimeData data;
	private final Instrumenter instrumenter;

	public InstrumentingLoader(IRuntime runtime, String scope,
			ClassLoader delegate) throws Exception {
		this.runtime = runtime;
		this.scope = scope;
		this.delegate = delegate;
		this.data = new RuntimeData();
		runtime.startup(data);
		this.instrumenter = new Instrumenter(runtime);
	}

	public InstrumentingLoader(Class<?> target) throws Exception {
		this(new SystemPropertiesRuntime(), target.getPackage().getName(),
				target.getClassLoader());
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		if (name.startsWith(scope)) {
			Class<?> c = findLoadedClass(name);
			if (c != null) {
				return c;
			}
			final byte[] bytes;
			try {
				bytes = TargetLoader.getClassDataAsBytes(delegate, name);
			} catch (IOException e) {
				throw new ClassNotFoundException("Unable to load", e);
			}
			final byte[] instrumented;
			try {
				instrumented = instrumenter.instrument(bytes, name);
			} catch (IOException e) {
				throw new ClassNotFoundException("Unable to instrument", e);
			}
			c = defineClass(name, instrumented, 0, instrumented.length);
			if (resolve) {
				resolveClass(c);
			}
			return c;
		}
		return super.loadClass(name, resolve);
	}

	public ExecutionDataStore collect() {
		final ExecutionDataStore store = new ExecutionDataStore();
		data.collect(store, new SessionInfoStore(), false);
		runtime.shutdown();
		return store;
	}

}
