/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link KotlinEnumFilter}.
 */
public class KotlinEnumFilterTest extends FilterTestBase {

	private final KotlinEnumFilter filter = new KotlinEnumFilter();

	/**
	 * <pre>
	 *   enum class Example {}
	 * </pre>
	 */
	@Test
	public void should_filter() {
		context.superClassName = "java/lang/Enum";
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "getEntries",
				"()Lkotlin/enums/EnumEntries;", null, null);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Example", "$ENTRIES",
				"Lkotlin/enums/EnumEntries;");
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	@Test
	public void should_not_filter_when_not_Enum() {
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "getEntries",
				"()Lkotlin/enums/EnumEntries;", null, null);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Example", "$ENTRIES",
				"Lkotlin/enums/EnumEntries;");
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_when_not_getEntries_name() {
		context.superClassName = "java/lang/Enum";
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "get",
				"()Lkotlin/enums/EnumEntries;", null, null);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Example", "$ENTRIES",
				"Lkotlin/enums/EnumEntries;");
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

	@Test
	public void should_not_filter_when_not_getEntries_descriptor() {
		context.superClassName = "java/lang/Enum";
		context.classAnnotations
				.add(KotlinGeneratedFilter.KOTLIN_METADATA_DESC);
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "getEntries",
				"(I)Lkotlin/enums/EnumEntries;", null, null);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Example", "$ENTRIES",
				"Lkotlin/enums/EnumEntries;");
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored();
	}

}
