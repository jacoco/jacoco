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
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit test for {@link KotlinSerializableFilter}.
 */
public class KotlinSerializableFilterTest extends FilterTestBase {

	private final IFilter filter = new KotlinSerializableFilter();

	/**
	 * <pre>
	 * &#064;kotlinx.serialization.Serializable
	 * data class Example(val data: String)
	 * </pre>
	 */
	@Test
	public void should_filter_synthetic_writeSelf_method() {
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC
						| Opcodes.ACC_SYNTHETIC,
				"write$Self$module_name",
				"(Lpkg$Example;Lkotlinx/serialization/encoding/CompositeEncoder;Lkotlinx/serialization/descriptors/SerialDescriptor;)V",
				null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * <pre>
	 * &#064;kotlinx.serialization.Serializable
	 * data class Example(val data: String)
	 * </pre>
	 */
	@Test
	public void should_filter_synthetic_constructor() {
		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, "<init>",
				"(ILjava/lang/String;Lkotlinx/serialization/internal/SerializationConstructorMarker;)V",
				null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * Kotlin 2.1.21 for
	 *
	 * <pre>
	 * &#064;kotlinx.serialization.Serializable // line 1
	 * data class Example(val data: String)
	 * </pre>
	 */
	@Test
	public void should_filter_generated_serializer_method() {
		context.className = "Example$Companion";

		final MethodNode initMethod = new MethodNode(Opcodes.ACC_PRIVATE,
				"<init>", "()V", null, null);
		// no line numbers
		filter.filter(initMethod, context, output);

		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "serializer",
				"()Lkotlinx/serialization/KSerializer;",
				"()Lkotlinx/serialization/KSerializer<LExample;>;", null);
		final Label label0 = new Label();
		m.visitLabel(label0);
		m.visitLineNumber(1, label0);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Example$$serializer", "INSTANCE",
				"LExample$$serializer;");
		m.visitTypeInsn(Opcodes.CHECKCAST, "kotlinx/serialization/KSerializer");
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * <pre>
	 * &#064;kotlinx.serialization.Serializable
	 * data class Example(val data: String) {
	 *     companion object // line 2
	 * }
	 * </pre>
	 */
	@Test
	public void should_filter_generated_serializer_method_in_hand_written_companion() {
		context.className = "Example$Companion";

		final MethodNode initMethod = new MethodNode(Opcodes.ACC_PRIVATE,
				"<init>", "()V", null, null);
		final Label initMethodLineNumberLabel = new Label();
		initMethod.visitLabel(initMethodLineNumberLabel);
		initMethod.visitLineNumber(2, initMethodLineNumberLabel);
		filter.filter(initMethod, context, output);

		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "serializer",
				"()Lkotlinx/serialization/KSerializer;",
				"()Lkotlinx/serialization/KSerializer<LExample;>;", null);
		final Label label0 = new Label();
		m.visitLabel(label0);
		m.visitLineNumber(2, label0);
		m.visitFieldInsn(Opcodes.GETSTATIC, "Example$$serializer", "INSTANCE",
				"LExample$$serializer;");
		m.visitTypeInsn(Opcodes.CHECKCAST, "kotlinx/serialization/KSerializer");
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * <pre>
	 * data class Example(val data: String) {
	 *     companion object { // line 2
	 *         fun serializer(): KSerializer&lt;Example&gt; = CustomSerializer
	 *     }
	 * }
	 * </pre>
	 */
	@Test
	public void should_not_filter_hand_written_serializer_method() {
		context.className = "Example$Companion";

		final MethodNode initMethod = new MethodNode(Opcodes.ACC_PRIVATE,
				"<init>", "()V", null, null);
		final Label initMethodLineNumberLabel = new Label();
		initMethod.visitLabel(initMethodLineNumberLabel);
		initMethod.visitLineNumber(2, initMethodLineNumberLabel);
		filter.filter(initMethod, context, output);

		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "serializer",
				"()Lkotlinx/serialization/KSerializer;",
				"()Lkotlinx/serialization/KSerializer<LExample;>;", null);
		final Label label0 = new Label();
		m.visitLabel(label0);
		m.visitLineNumber(3, label0);
		m.visitFieldInsn(Opcodes.GETSTATIC, "CustomSerializer", "INSTANCE",
				"LCustomSerializer;");
		m.visitTypeInsn(Opcodes.CHECKCAST, "kotlinx/serialization/KSerializer");
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertIgnored(m);
	}

	/**
	 * <code>Example.serializer</code> in case of
	 *
	 * <pre>
	 * &#064;kotlinx.serialization.Serializable // line 1
	 * object Example
	 * </pre>
	 *
	 * <code>Example$Companion.serializer</code> in case of
	 *
	 * <pre>
	 * &#064;kotlinx.serialization.Serializable // line 1
	 * enum class Example {
	 *     V
	 * }
	 * </pre>
	 *
	 * <pre>
	 * &#064;kotlinx.serialization.Serializable // line 1
	 * sealed class Example
	 * </pre>
	 */
	@Test
	public void should_filter_generated_serializer_method_in_objects_and_companions_of_enum_and_sealed_class() {
		context.className = "Example";

		final MethodNode m = new MethodNode(
				Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "serializer",
				"()Lkotlinx/serialization/KSerializer;",
				"()Lkotlinx/serialization/KSerializer<LExample;>;", null);
		final Label label0 = new Label();
		m.visitLabel(label0);
		m.visitLineNumber(1, label0);
		m.visitVarInsn(Opcodes.ALOAD, 0);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "Example",
				"get$cachedSerializer", "()Lkotlinx/serialization/KSerializer;",
				false);
		m.visitInsn(Opcodes.ARETURN);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * <code>Example.get$cachedSerializer</code> in case of
	 *
	 * <pre>
	 * &#064;kotlinx.serialization.Serializable
	 * object Example
	 * </pre>
	 *
	 * <code>Example$Companion.get$cachedSerializer</code> in case of
	 *
	 * <pre>
	 * &#064;kotlinx.serialization.Serializable
	 * enum class Example {
	 *     V
	 * }
	 * </pre>
	 *
	 * <pre>
	 * &#064;kotlinx.serialization.Serializable
	 * sealed class Example
	 * </pre>
	 */
	@Test
	public void should_filter_synthetic_get_cachedSerializer_method() {
		context.className = "Example";

		MethodNode m = new MethodNode(
				Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC,
				"get$cachedSerializer", "()Lkotlinx/serialization/KSerializer;",
				null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * <pre>
	 * &#064;kotlinx.serialization.Serializable
	 * object Example
	 * </pre>
	 *
	 * <pre>
	 * &#064;kotlinx.serialization.Serializable
	 * enum class Example
	 * </pre>
	 *
	 * <pre>
	 * &#064;kotlinx.serialization.Serializable
	 * sealed class Example
	 * </pre>
	 *
	 * lazy initializer
	 *
	 * <pre>
	 * $cachedSerializer$delegate = lazy { ... }
	 * </pre>
	 *
	 * not executed when serializer is not used
	 *
	 * https://github.com/JetBrains/kotlin/commit/3f034e8b6735a50ed5733e82811fc2bdb73f5632
	 */
	@Test
	public void should_filter_synthetic_lazy_cachedSerializer() {
		context.className = "Example";

		final MethodNode m = new MethodNode(
				Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC
						| Opcodes.ACC_SYNTHETIC,
				"_init_$_anonymous_", "()Lkotlinx/serialization/KSerializer;",
				null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

	/**
	 * <pre>
	 * &#064;kotlinx.serialization.Serializable
	 * data class Example(
	 *     val data1: List&lt;String&gt;,
	 *     val data2: List&lt;String&gt;,
	 * )
	 * </pre>
	 *
	 * lazy initializer
	 *
	 * <pre>
	 * $childSerializers = arrayOf(lazy { ... }, lazy { ... })
	 * </pre>
	 *
	 * not executed when serializer is not used
	 *
	 * https://github.com/JetBrains/kotlin/commit/b35161e241df6a7b245b5a5d81232b0ea5a3129a
	 * https://github.com/JetBrains/kotlin/commit/3f034e8b6735a50ed5733e82811fc2bdb73f5632
	 */
	@Test
	public void should_filter_synthetic_lazy_childSerializers() {
		context.className = "Example";

		final MethodNode m = new MethodNode(
				Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC
						| Opcodes.ACC_SYNTHETIC,
				"_childSerializers$_anonymous_$0",
				"()Lkotlinx/serialization/KSerializer;", null, null);
		m.visitInsn(Opcodes.NOP);

		filter.filter(m, context, output);

		assertMethodIgnored(m);
	}

}
