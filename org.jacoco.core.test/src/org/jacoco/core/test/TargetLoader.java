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
package org.jacoco.core.test;

import java.io.InputStream;

/**
 * Loads a single class from a byte array.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class TargetLoader {

	private final Class<?> clazz;

	public TargetLoader(final String name, final byte[] bytes) {
		clazz = load(name, bytes);
	}

	public TargetLoader(final Class<?> source, final byte[] bytes) {
		clazz = load(source.getName(), bytes);
	}

	private Class<?> load(final String sourcename, final byte[] bytes) {
		final ClassLoader cl = new ClassLoader(this.getClass().getClassLoader()) {

			@Override
			protected synchronized Class<?> loadClass(String name,
					boolean resolve) throws ClassNotFoundException {
				if (sourcename.equals(name)) {
					Class<?> c = defineClass(name, bytes, 0, bytes.length);
					if (resolve) {
						resolveClass(c);
					}
					return c;
				}
				return super.loadClass(name, resolve);
			}
		};
		try {
			return cl.loadClass(sourcename);
		} catch (ClassNotFoundException e) {
			// must not happen
			throw new RuntimeException(e);
		}
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public Object newInstance() throws InstantiationException,
			IllegalAccessException {
		return clazz.newInstance();
	}

	public static InputStream getClassData(Class<?> clazz) {
		final String resource = "/" + clazz.getName().replace('.', '/')
				+ ".class";
		return clazz.getResourceAsStream(resource);
	}

}
