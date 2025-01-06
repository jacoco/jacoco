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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters methods that Kotlin compiler generates for inline classes.
 *
 * For
 *
 * <pre>
 * &#064;kotlin.jvm.JvmInline
 * value class Example(val value: String) : Base {
 *   fun f(p: String) { ... }
 *   fun f(p: Example) { ... }
 *   override fun base() { ... }
 * }
 * </pre>
 *
 * Kotlin compiler produces
 *
 * <pre>
 * &#064;kotlin.jvm.JvmInline
 * class Example implements Base {
 *   private final String value;
 *   public String getValue() { return value; }
 *
 *   private synthetic Example(String value) { this.value = value; }
 *
 *   public static String constructor-impl(String value) { ... }
 *
 *   public static void f-impl(String value, String p) { ... }
 *
 *   public static void f-ulP-heY(String value, String p) { ... }
 *
 *   public void base() { base-impl(value); }
 *   public static void base-impl(String value) { ... }
 *
 *   public String toString() { return toString-impl(value); }
 *   public static String toString-impl(String value) { ... }
 *
 *   public boolean equals(Object other) { return equals-impl(value, other); }
 *   public static boolean equals-impl(String value, Object other) { ... }
 *
 *   public int hashCode() { return hashCode-impl(value); }
 *   public static int hashCode-impl(String value) { ... }
 *
 *   public final synthetic String unbox-impl() { return value; }
 *   public static synthetic Example box-impl(String value) { return new Example(value); }
 *
 *   public static equals-impl0(String value1, String value2) { ... }
 * }
 * </pre>
 *
 * Except getter all non-synthetic non-static methods delegate to corresponding
 * static methods. Non-static methods are provided for interoperability with
 * Java and can not be invoked from Kotlin without reflection and so should be
 * filtered out.
 */
final class KotlinInlineClassFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!context.getClassAnnotations().contains("Lkotlin/jvm/JvmInline;")) {
			return;
		}
		if ((methodNode.access & Opcodes.ACC_STATIC) != 0) {
			return;
		}
		output.ignore(methodNode.instructions.getFirst(),
				methodNode.instructions.getLast());
	}

}
