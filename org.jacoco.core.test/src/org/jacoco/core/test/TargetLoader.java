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
package org.jacoco.core.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads a single class from a byte array.
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

	public Class<?> add(final Class<?> source) throws IOException {
		return add(source.getName(), getClassDataAsBytes(source));
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
		final String resource = "/" + clazz.getName().replace('.', '/')
				+ ".class";
		return clazz.getResourceAsStream(resource);
	}

	public static byte[] getClassDataAsBytes(Class<?> clazz) throws IOException {
		InputStream in = getClassData(clazz);
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
