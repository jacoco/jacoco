/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinInlineFilter}.
 */
public class KotlinInlineFilterTest extends FilterTestBase {

	private final KotlinInlineFilter filter = new KotlinInlineFilter();

	private final MethodNode m = new MethodNode(InstrSupport.ASM_API_VERSION, 0,
			"callsite", "()V", null, null);

	@Test
	public void should_filter() {
		context.sourceDebugExtension = "" //
				+ "SMAP\n" //
				+ "callsite.kt\n" //
				+ "Kotlin\n" //
				+ "*S Kotlin\n" //
				+ "*F\n" //
				+ "+ 1 callsite.kt\n" //
				+ "CallsiteKt\n" //
				+ "+ 2 a.kt\n" //
				+ "AKt\n" //
				+ "+ 3 b.kt\n" //
				+ "BKt\n" //
				+ "*L\n" //
				+ "1#1,8:1\n" //
				+ "2#2,2:9\n" //
				+ "2#3,2:11\n" //
				+ "*E\n" //
				+ "*S KotlinDebug\n" //
				+ "*F\n" //
				+ "+ 1 callsite.kt\n" //
				+ "CallsiteKt\n" //
				+ "*L\n" //
				+ "2#1,2:9\n" //
				+ "3#1,2:11\n" //
				+ "*E";
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		m.visitLineNumber(2, new Label());
		m.visitInsn(Opcodes.NOP);

		m.visitLineNumber(9, new Label());
		shouldIgnorePrevious(m);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Stubs", "nop", "()V", false);
		shouldIgnorePrevious(m);
		m.visitLineNumber(10, new Label());
		shouldIgnorePrevious(m);
		m.visitInsn(Opcodes.NOP);
		shouldIgnorePrevious(m);

		m.visitLineNumber(3, new Label());
		m.visitInsn(Opcodes.NOP);

		m.visitLineNumber(11, new Label());
		shouldIgnorePrevious(m);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "Stubs", "nop", "()V", false);
		shouldIgnorePrevious(m);
		m.visitLineNumber(12, new Label());
		shouldIgnorePrevious(m);
		m.visitInsn(Opcodes.NOP);
		shouldIgnorePrevious(m);

		m.visitLineNumber(4, new Label());
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored(expectedRanges.toArray(new Range[0]));

		// should not reparse:
		context.sourceDebugExtension = "";
		filter.filter(m, context, output);
	}

	@Test
	public void should_not_parse_SourceDebugExtension_attribute_when_no_kotlin_metadata_annotation() {
		context.sourceDebugExtension = "SMAP";

		m.visitLineNumber(1, new Label());
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_when_no_SourceDebugExtension_attribute() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		m.visitLineNumber(1, new Label());
		m.visitInsn(Opcodes.RETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_throw_exception_when_SMAP_incomplete() {
		context.sourceDebugExtension = "" //
				+ "SMAP\n" //
				+ "callsite.kt\n" //
				+ "Kotlin\n" //
				+ "*S Kotlin\n" //
				+ "*F\n";
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);

		try {
			filter.filter(m, context, output);
			fail("exception expected");
		} catch (final IllegalStateException e) {
			assertEquals("Unexpected SMAP line: null", e.getMessage());
		}
	}

	private final List<Range> expectedRanges = new ArrayList<Range>();

	private void shouldIgnorePrevious(final MethodNode m) {
		expectedRanges.add(
				new Range(m.instructions.getLast(), m.instructions.getLast()));
	}

}
