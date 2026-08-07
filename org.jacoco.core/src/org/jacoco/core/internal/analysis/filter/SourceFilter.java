/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.analysis.ISourceFileProvider;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filter that excludes lines based on <code>//jacoco:off</code> and
 * <code>//jacoco:on</code> comments in the source file.
 */
public class SourceFilter implements IFilter {

	private final ISourceFileProvider sourceProvider;
	private final Map<String, BitSet> cache = new HashMap<String, BitSet>();

	/**
	 * Creates a new filter using the given source provider.
	 *
	 * @param sourceProvider
	 *            provider to retrieve source content
	 */
	public SourceFilter(final ISourceFileProvider sourceProvider) {
		this.sourceProvider = sourceProvider;
	}

	@Override
	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		final String packageName = getPackageName(context.getClassName());
		final String sourceFileName = context.getSourceFileName();
		if (sourceFileName == null) {
			return;
		}

		final String key = packageName + "/" + sourceFileName;
		final BitSet ignoreLines;
		if (cache.containsKey(key)) {
			ignoreLines = cache.get(key);
		} else {
			BitSet newIgnoreLines = null;
			try {
				final Reader reader = sourceProvider.getSourceFile(packageName,
						sourceFileName);
				if (reader != null) {
					newIgnoreLines = parse(reader);
				}
			} catch (final IOException e) {
				// If source cannot be read we just continue without filtering
			}
			ignoreLines = newIgnoreLines;
			cache.put(key, ignoreLines);
		}

		if (ignoreLines == null) {
			return;
		}

		int currentLine = -1;
		for (AbstractInsnNode i = methodNode.instructions
				.getFirst(); i != null; i = i.getNext()) {
			if (AbstractInsnNode.LINE == i.getType()) {
				currentLine = ((LineNumberNode) i).line;
			}
			if (currentLine != -1 && ignoreLines.get(currentLine)
					&& AbstractInsnNode.LINE != i.getType()) {
				output.ignore(i, i);
			}
		}
	}

	private BitSet parse(final Reader reader) throws IOException {
		final BitSet ignoreLines = new BitSet();
		final BufferedReader buffer = new BufferedReader(reader);
		String line;
		int nr = 1;
		boolean ignore = false;
		while ((line = buffer.readLine()) != null) {
			if (line.contains("jacoco:on")) {
				ignore = false;
			}
			if (ignore || line.contains("jacoco:ignore")) {
				ignoreLines.set(nr);
			}
			if (line.contains("jacoco:off")) {
				ignore = true;
			}
			nr++;
		}
		return ignoreLines;
	}

	private String getPackageName(final String className) {
		final int pos = className.lastIndexOf('/');
		return pos == -1 ? "" : className.substring(0, pos);
	}

}
