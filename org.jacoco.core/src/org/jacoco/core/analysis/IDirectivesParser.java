/*******************************************************************************
 * Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Martin Hare Robertson - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Queue;

import org.jacoco.core.data.ISourceFileLocator;

/**
 * Parser of coverage directives
 */
public interface IDirectivesParser {

	/**
	 * Data class representing a directive
	 */
	public static class Directive {
		/**
		 * @param lineNum
		 * @param coverageOn
		 */
		public Directive(final int lineNum, final boolean coverageOn) {
			this.lineNum = lineNum;
			this.coverageOn = coverageOn;
		}

		/**
		 * Line number of the directive
		 */
		public final int lineNum;
		/**
		 * Whether to switch coverage on/off
		 */
		public final boolean coverageOn;
	}

	/**
	 * Parser for directives in source code
	 */
	public static class SourceFileDirectivesParser implements IDirectivesParser {
		private final ISourceFileLocator sourceLocator;

		/**
		 * @param sourceLocator
		 *            Object for locating source code
		 */
		public SourceFileDirectivesParser(final ISourceFileLocator sourceLocator) {
			this.sourceLocator = sourceLocator;
		}

		public Queue<Directive> parseDirectives(final String packageName,
				final String sourceFilename) {
			final Queue<Directive> directives = new LinkedList<Directive>();

			try {
				final Reader sourceReader = sourceLocator.getSourceFile(
						packageName, sourceFilename);

				if (sourceReader != null) {
					final BufferedReader bufSourceReader = new BufferedReader(
							sourceReader);
					try {
						int lineNum = 1;
						String line;
						while ((line = bufSourceReader.readLine()) != null) {
							if (line.trim().equals("///JACOCO:OFF")) {
								directives.add(new Directive(lineNum, false));
							} else if (line.trim().equals("///JACOCO:ON")) {
								directives.add(new Directive(lineNum, true));
							}
							lineNum++;
						}
					} finally {
						bufSourceReader.close();
					}
				}
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}

			return directives;
		}

	}

	/**
	 * Return coverage directives associated with the specified className
	 * 
	 * @param packageName
	 * @param sourceFilename
	 * @return Queue of directives in the order which they apply.
	 */
	public Queue<IDirectivesParser.Directive> parseDirectives(
			String packageName, String sourceFilename);
}