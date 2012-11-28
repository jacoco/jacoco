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
package org.jacoco.core.analysis.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Queue;

import org.jacoco.core.analysis.IDirectivesParser;
import org.jacoco.core.analysis.IDirectivesParser.Directive;
import org.jacoco.core.data.ISourceFileLocator;
import org.junit.Test;

public class SourceFileDirectiveParserTest {
	@Test
	public void testSourceParsing() {
		ISourceFileLocator locator = new ISourceFileLocator() {
			public int getTabWidth() {
				return 0;
			}

			public Reader getSourceFile(String packageName, String fileName)
					throws IOException {
				return new StringReader("\n" + "  ///JACOCO:OFF  \n" + "\n"
						+ "  ///JACOCO:ON   \n");
			}
		};
		IDirectivesParser.SourceFileDirectivesParser parser = new IDirectivesParser.SourceFileDirectivesParser(
				locator);
		Queue<Directive> directives = parser.parseDirectives(null, null);

		assertEquals(2, directives.size());
		assertEquals(2, directives.peek().lineNum);
		assertFalse(directives.peek().coverageOn);
		directives.poll();
		assertEquals(4, directives.peek().lineNum);
		assertTrue(directives.peek().coverageOn);
	}

	@Test
	public void testMissingSourceParsing() {
		ISourceFileLocator locator = new ISourceFileLocator() {
			public int getTabWidth() {
				return 0;
			}

			public Reader getSourceFile(String packageName, String fileName)
					throws IOException {
				return null;
			}
		};
		IDirectivesParser.SourceFileDirectivesParser parser = new IDirectivesParser.SourceFileDirectivesParser(
				locator);
		Queue<Directive> directives = parser.parseDirectives(null, null);

		assertEquals(0, directives.size());
	}
}
