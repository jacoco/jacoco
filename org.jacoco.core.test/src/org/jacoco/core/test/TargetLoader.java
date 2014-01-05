/*******************************************************************************
 * Copyright (c) 2009, 2014 Mountainminds GmbH & Co. KG and Contributors
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

/**
 * Loads a single class from a byte array.
 */
public class TargetLoader extends ClassLoader {

	private final String sourcename;

	private final byte[] bytes;

	private final Class<?> clazz;

	public TargetLoader(final String name, final byte[] bytes) {
		super(TargetLoader.class.getClassLoader());
		this.sourcename = name;
		this.bytes = bytes;
		clazz = load(name);
	}

	public TargetLoader(final Class<?> source, final byte[] bytes) {
		super(TargetLoader.class.getClassLoader());
		this.sourcename = source.getName();
		this.bytes = bytes;
		clazz = load(source.getName());
	}

	private Class<?> load(final String sourcename) {
		try {
			return loadClass(sourcename);
		} catch (ClassNotFoundException e) {
			// must not happen
			throw new RuntimeException(e);
		}
	}

	public Class<?> getTargetClass() {
		return clazz;
	}

	public Object newTargetInstance() throws InstantiationException,
			IllegalAccessException {
		return clazz.newInstance();
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
		if (sourcename.equals(name)) {
			Class<?> c = defineClass(name, bytes, 0, bytes.length);
			if (resolve) {
				resolveClass(c);
			}
			return c;
		}
		return super.loadClass(name, resolve);
	}

}
