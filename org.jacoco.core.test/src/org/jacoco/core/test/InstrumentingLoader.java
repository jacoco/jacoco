/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test;

import java.io.IOException;
import java.io.InputStream;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.runtime.SystemPropertiesRuntime;

public final class InstrumentingLoader extends ClassLoader {

	private final RuntimeData data;
	private final IRuntime runtime;

	public InstrumentingLoader() throws Exception {
		data = new RuntimeData();
		runtime = new SystemPropertiesRuntime();
		runtime.startup(data);
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		if (name.startsWith("org.jacoco.core.test.validation.targets.")) {
			final byte[] bytes;
			try {
				bytes = getClassBytes(name);
			} catch (IOException e) {
				throw new ClassNotFoundException("Unable to load", e);
			}
			final byte[] instrumented;
			try {
				instrumented = new Instrumenter(runtime).instrument(bytes,
						name);
			} catch (IOException e) {
				throw new ClassNotFoundException("Unable to instrument", e);
			}
			final Class<?> c = defineClass(name, instrumented, 0,
					instrumented.length);
			if (resolve) {
				resolveClass(c);
			}
			return c;
		}
		return super.loadClass(name, resolve);
	}

	public byte[] getClassBytes(String name) throws IOException {
		final String resource = "/" + name.replace('.', '/') + ".class";
		final InputStream in = getClass().getResourceAsStream(resource);
		return TargetLoader.getClassDataAsBytes(in);
	}

	public ExecutionDataStore collect() {
		final ExecutionDataStore store = new ExecutionDataStore();
		data.collect(store, new SessionInfoStore(), false);
		runtime.shutdown();
		return store;
	}

}
