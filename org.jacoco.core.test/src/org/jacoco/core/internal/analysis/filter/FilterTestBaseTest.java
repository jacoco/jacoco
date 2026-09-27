/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.test.TextBlock;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link FilterTestBase}.
 */
public class FilterTestBaseTest extends FilterTestBase {

	@Test
	public void assertSnapshot_should_throw_when_invalid_comment()
			throws Exception {
		try {
			assertSnapshot(null, context, new StringReader(TextBlock.lines( //
					"// first comment ignored", //
					"RETURN", //
					"// foo")));
			fail("IllegalStateException expected");
		} catch (final IllegalStateException e) {
			assertEquals("Invalid syntax: // foo", e.getMessage());
		}
	}

	@Test
	public void assertSnapshot_should_throw_when_no_range_start()
			throws Exception {
		try {
			assertSnapshot(null, context, new StringReader(TextBlock.lines( //
					"RETURN", //
					"// previous instruction ends ignore range NAME")));
			fail("IllegalStateException expected");
		} catch (final IllegalStateException e) {
			assertEquals("Missing start for range NAME", e.getMessage());
		}
	}

	@Test
	public void assertSnapshot_should_throw_when_no_range_end()
			throws Exception {
		try {
			assertSnapshot(null, context, new StringReader(TextBlock.lines( //
					"RETURN", //
					"// previous instruction starts ignore range NAME")));
			fail("IllegalStateException expected");
		} catch (final IllegalStateException e) {
			assertEquals("Missing end for range NAME", e.getMessage());
		}
	}

	@Test
	public void assertSnapshot_should_throw_when_duplicate_range_start()
			throws Exception {
		try {
			assertSnapshot(null, context, new StringReader(TextBlock.lines( //
					"RETURN", //
					"// previous instruction starts ignore range NAME", //
					"// previous instruction starts ignore range NAME")));
			fail("IllegalStateException expected");
		} catch (final IllegalStateException e) {
			assertEquals("Duplicate start for range NAME", e.getMessage());
		}
	}

	@Test
	public void assertSnapshot_should_throw_when_duplicate_range_end()
			throws Exception {
		try {
			assertSnapshot(null, context, new StringReader(TextBlock.lines( //
					"RETURN", //
					"// previous instruction starts ignore range NAME", //
					"// previous instruction ends ignore range NAME", //
					"// previous instruction ends ignore range NAME")));
			fail("IllegalStateException expected");
		} catch (final IllegalStateException e) {
			assertEquals("Duplicate end for range NAME", e.getMessage());
		}
	}

	@Test
	public void assertSnapshot_should_throw_when_no_branches_for_replacement()
			throws Exception {
		try {
			assertSnapshot(null, context, new StringReader(TextBlock.lines( //
					"RETURN", //
					"// previous instruction replaced by NAME")));
			fail("IllegalStateException expected");
		} catch (final IllegalStateException e) {
			assertEquals("Missing branches for replacement NAME",
					e.getMessage());
		}
	}

	@Test
	public void assertSnapshot_should_compare_ranges() throws Exception {
		final IFilter filter = new IFilter() {
			public void filter(final MethodNode methodNode,
					final IFilterContext context, final IFilterOutput output) {
				output.ignore(methodNode.instructions.getFirst(),
						methodNode.instructions.getLast());
			}
		};
		try {
			assertSnapshot(filter, context, new StringReader(TextBlock.lines( //
					"NOP", //
					"// previous instruction starts ignore range NAME_BB", //
					"// previous instruction ends ignore range NAME_BB", //
					"NOP", //
					"// previous instruction starts ignore range NAME_Aa", //
					"// previous instruction ends ignore range NAME_Aa", //
					"RETURN")));
			fail("ComparisonFailure expected");
		} catch (final ComparisonFailure e) {
			assertTrue(e.getMessage().startsWith("ignored ranges"));
			assertEquals(TextBlock.lines( //
					"range 0 from instruction 0 to 2"), //
					e.getActual());
			assertEquals(TextBlock.lines( //
					"range 0 from instruction 1 to 1", //
					"range 1 from instruction 0 to 0"), //
					e.getExpected());
		}
	}

	@Test
	public void assertSnapshot_should_compare_replacements() throws Exception {
		final IFilter filter = new IFilter() {
			public void filter(final MethodNode methodNode,
					final IFilterContext context, final IFilterOutput output) {
				final Replacements replacements = new Replacements();
				replacements.add(methodNode.instructions.getFirst(),
						methodNode.instructions.getFirst(), 0);
				output.replaceBranches(methodNode.instructions.getFirst(),
						replacements);
			}
		};
		try {
			assertSnapshot(filter, context, new StringReader(TextBlock.lines( //
					"IFEQ LABEL", // ,
					"// previous instruction replaced by NAME", //
					"// previous instruction branch 0 creates branch 1 for NAME",
					"// previous instruction branch 1 creates branch 0 for NAME")));
			fail("ComparisonFailure expected");
		} catch (final ComparisonFailure e) {
			assertTrue(e.getMessage().startsWith("replacements"));
			assertEquals("[\n" + //
					"0 if branch 0 of instruction 0]", e.getActual());
			assertEquals("[\n" + //
					"0 if branch 1 of instruction 0, \n" + //
					"1 if branch 0 of instruction 0]", //
					e.getExpected());
		}
	}

	@Test
	public void assertIgnored_should_throw_ComparisonFailure() {
		final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
				"example", "()V", null, null);
		final Range range = new Range();
		m.visitInsn(Opcodes.NOP);
		range.fromInclusive = m.instructions.getFirst();
		range.toInclusive = m.instructions.getLast();

		try {
			assertIgnored(m, range);
			fail("exception expected");
		} catch (final ComparisonFailure e) {
			assertEquals("", e.getActual());
			assertEquals("range 0 from instruction 0 to 0\n", e.getExpected());
			assertTrue(e.getMessage().startsWith("ignored ranges expected:"));
		}
	}

}
