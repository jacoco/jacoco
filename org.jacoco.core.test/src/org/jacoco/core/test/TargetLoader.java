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
package org.jacoco.core.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads given classes from a byte arrays.
 */
public class TargetLoader extends ClassLoader {

	private final Map<String, byte[]> classes;

	public TargetLoader() {
		super(TargetLoader.class.getClassLoader());
		this.classes = new HashMap<String, byte[]>();
	}

	public Class<?> add(final String name, final byte[] bytes) {
		this.classes.put(name, bytes);
		return load(name);
	}

	public Class<?> add(final Class<?> name, final byte[] bytes) {
		return add(name.getName(), bytes);
	}

	private Class<?> load(final String sourcename) {
		try {
			return loadClass(sourcename);
		} catch (ClassNotFoundException e) {
			// must not happen
			throw new RuntimeException(e);
		}
	}

	public static InputStream getClassData(Class<?> clazz) {
		return getClassData(clazz.getClassLoader(), clazz.getName());
	}

	public static InputStream getClassData(ClassLoader loader, String name) {
		final String resource = name.replace('.', '/') + ".class";
		return loader.getResourceAsStream(resource);
	}

	public static byte[] getClassDataAsBytes(ClassLoader loader, String name)
			throws IOException {
		return readBytes(getClassData(loader, name));
	}

	public static byte[] getClassDataAsBytes(Class<?> clazz)
			throws IOException {
		return readBytes(getClassData(clazz));
	}

	private static byte[] readBytes(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[0x100];
		int len;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		in.close();
		return out.toByteArray();
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		final byte[] bytes = classes.get(name);
		if (bytes != null) {
			Class<?> c = defineClass(name, bytes, 0, bytes.length);
			if (resolve) {
				resolveClass(c);
			}
			return c;
		}
		return super.loadClass(name, resolve);
	}

}
