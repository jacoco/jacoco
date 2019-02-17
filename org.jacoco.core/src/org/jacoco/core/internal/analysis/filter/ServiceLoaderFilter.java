/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergey Zhemzhitsky - initial API and implementation
 *
 *******************************************************************************/

package org.jacoco.core.internal.analysis.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.objectweb.asm.tree.MethodNode;

/**
 * Loads additional filters the same way {@code java.util.ServiceLoader}
 * from Java 6 does, staying compatible with Java 5 API.
 * For the additional filters to be found they should be accessible by the same
 * {@code ClassLoader} that loads this class.
 */
public class ServiceLoaderFilter implements IFilter {

	private static final String RESOURCE_NAME =
			"META-INF/services/" + IFilter.class.getName();
	private static final char COMMENT_CHAR = '#';

	private final ClassLoader loader = getClass().getClassLoader();

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		final Enumeration<URL> resources = findResources(loader);
		while (resources.hasMoreElements()) {
			final URL resource = resources.nextElement();
			final Collection<String> serviceClasses = readURL(resource);
			for (final String serviceClass : serviceClasses) {
				IFilter filter = loadService(serviceClass, loader);
				filter.filter(methodNode, context, output);
			}
		}
	}

	private static Enumeration<URL> findResources(final ClassLoader loader) {
		try {
			return loader.getResources(RESOURCE_NAME);
		} catch (IOException e) {
			throw new IllegalStateException(
					"Unable to get a list of resources: " + RESOURCE_NAME, e);
		}
	}

	private static Collection<String> readURL(final URL url) {
		Set<String> lines = new LinkedHashSet<String>();

		Exception failReason = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(
					new InputStreamReader(url.openStream(), "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				final String parsedLine = parseLine(url, line);
				if (parsedLine != null) {
					lines.add(parsedLine);
				}
			}
		} catch (IOException e1) {
			failReason = e1;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e2) {
					if (failReason != null) {
						failReason = e2;
					}
				}
			}
		}

		if (failReason != null) {
			throw new IllegalStateException(
					"Unable to load a list of services from URL: " + url,
					failReason);
		}

		return lines;
	}

	private static String parseLine(final URL url, final String line) {
		String parsedLine = line.trim();
		final int commentInd = parsedLine.indexOf(COMMENT_CHAR);
		if (commentInd >= 0) {
			parsedLine = parsedLine.substring(0, commentInd);
		}
		parsedLine = parsedLine.trim();
		if (parsedLine.length() == 0) {
			return null;
		}

		int codePoint = parsedLine.codePointAt(0);
		if (!Character.isJavaIdentifierStart(codePoint)) {
			throw new IllegalStateException("Illegal service-class name; "
					+ "name must start from the valid java identifier: "
					+ url + ", " + parsedLine);
		}
		for (int i = Character.charCount(codePoint),
				length = parsedLine.length(); i < length;
				i += Character.charCount(codePoint)) {
			codePoint = parsedLine.codePointAt(i);
			if (!Character.isJavaIdentifierPart(codePoint)
					&& (codePoint != '.')) {
				throw new IllegalStateException("Illegal service-class name; "
						+ "name must consists of valid java identifier chars: "
						+ url + ", " + parsedLine);
			}
		}

		return parsedLine;
	}

	private static IFilter loadService(final String serviceClass,
			final ClassLoader loader) {
		try {
			Class<?> filterClass = Class.forName(serviceClass, false, loader);
			if (!IFilter.class.isAssignableFrom(filterClass)) {
				throw new IllegalStateException(
						"Filter class is not an instance of " +
								IFilter.class.getName());
			}
			return (IFilter) filterClass.newInstance();
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(
					"Filter class cannot be found: " + serviceClass, e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(
					"Filter class or its default constructor " +
							"is not accessible: " + serviceClass, e);
		} catch (InstantiationException e) {
			throw new IllegalStateException(
					"Filter class cannot be instantiated: " + serviceClass, e);
		}
	}

}
