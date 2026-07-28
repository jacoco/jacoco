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
package org.jacoco.core.internal.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jacoco.core.internal.flow.MethodProbesVisitor;
import org.jacoco.core.internal.instr.InstrSupport;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link ClassAnalyzer}.
 */
public class ClassAnalyzerTest {

	private ClassAnalyzer analyzer;
	private ClassCoverageImpl coverage;

	@Before
	public void setup() {
		coverage = new ClassCoverageImpl("Foo", 0x0000, false);
		analyzer = new ClassAnalyzer(coverage, null, new StringPool());
		analyzer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Foo", null,
				"java/lang/Object", null);
	}

	@Test
	public void should_throw_IllegalStateException_when_class_is_instrumented_with_data_field() {
		try {
			analyzer.visitField(InstrSupport.DATAFIELD_ACC,
					InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC,
					null, null);
			fail("IllegalStateException expected");
		} catch (final IllegalStateException e) {
			// expected
			assertEquals(
					"Cannot process instrumented class Foo. Please supply original non-instrumented classes.",
					e.getMessage());
			assertNull(e.getCause());
		}
	}

	@Test
	public void should_throw_IllegalStateException_when_class_is_instrumented_with_init_method() {
		try {
			analyzer.visitMethod(InstrSupport.INITMETHOD_ACC,
					InstrSupport.INITMETHOD_NAME, InstrSupport.INITMETHOD_DESC,
					null, null);
			fail("IllegalStateException expected");
		} catch (final IllegalStateException e) {
			// expected
			assertEquals(
					"Cannot process instrumented class Foo. Please supply original non-instrumented classes.",
					e.getMessage());
			assertNull(e.getCause());
		}
	}

	/**
	 * @see #should_add_non_empty_methods()
	 */
	@Test
	public void should_not_add_empty_methods() {
		final MethodNode m = new MethodNode(0, "foo", "()V", null, null);

		final MethodProbesVisitor mv = analyzer.visitMethod(m.access, m.name,
				m.desc, m.signature, m.exceptions.toArray(new String[0]));
		mv.accept(m, mv);

		assertEquals(0, coverage.getMethods().size());
	}

	/**
	 * @see #should_not_add_empty_methods()
	 */
	@Test
	public void should_add_non_empty_methods() {
		final MethodNode m = new MethodNode(0, "foo", "()V", null, null);
		m.visitInsn(Opcodes.RETURN);

		final MethodProbesVisitor mv = analyzer.visitMethod(m.access, m.name,
				m.desc, m.signature, m.exceptions.toArray(new String[0]));
		mv.accept(m, mv);

		assertEquals(1, coverage.getMethods().size());
	}

	@Test
	public void should_collect_annotations() {
		assertTrue(analyzer.getClassAnnotations().isEmpty());
		analyzer.visitAnnotation("Lpkg/Annotation;", true);
		assertTrue(analyzer.getClassAnnotations().contains("Lpkg/Annotation;"));
	}

	@Test
	public void should_collect_attributes() {
		assertTrue(analyzer.getClassAttributes().isEmpty());
		analyzer.visitAttribute(new Attribute("foo") {
		});
		assertTrue(analyzer.getClassAttributes().contains("foo"));
	}

	/**
	 * <pre>
	 * class Foo {
	 *   inline fun f() {}
	 *   fun g() = f()
	 * }
	 * </pre>
	 */
	@Test
	public void testCalculateFragments() {
		analyzer.visitSource("Foo.kt", "SMAP\n" //
				+ "Foo.kt\n" //
				+ "Kotlin\n" //
				+ "*S Kotlin\n" //
				+ "*F\n" //
				+ "+ 1 Foo.kt\n" //
				+ "Foo\n" //
				+ "*L\n" //
				+ "1#1,4:1\n" //
				+ "2#1:6\n" //
				+ "*E\n");
		analyzer.visitAnnotation("Lkotlin/Metadata;", false);
		MethodNode mn = new MethodNode(InstrSupport.ASM_API_VERSION, 0, "foo",
				"()V", null, null) {
			{
				final Label label = new Label();
				visitLabel(label);
				visitLineNumber(2, label);
				visitInsn(Opcodes.RETURN);
			}
		};
		MethodProbesVisitor mv = analyzer.visitMethod(mn.access, mn.name,
				mn.desc, mn.signature, mn.exceptions.toArray(new String[0]));
		mv.accept(mn, mv);
		mn = new MethodNode(InstrSupport.ASM_API_VERSION, 0, "bar", "()V", null,
				null) {
			{
				final Label label = new Label();
				visitLabel(label);
				visitLineNumber(6, label);
				visitInsn(Opcodes.RETURN);
			}
		};
		mv = analyzer.visitMethod(mn.access, mn.name, mn.desc, mn.signature,
				mn.exceptions.toArray(new String[0]));
		mv.accept(mn, mv);
		analyzer.visitEnd();

		final SourceNodeImpl fragment = coverage.getFragments().iterator()
				.next();
		assertEquals(fragment.getName(), "Foo");
		assertEquals(CounterImpl.COUNTER_1_0,
				fragment.getLine(2).getInstructionCounter());
	}

	/**
	 * When non-Kotlin SMAP.
	 *
	 * @see #should_not_parse_absent_SourceDebugExtension_attribute_when_kotlin()
	 * @see #should_parse_SourceDebugExtension_attribute_when_Kotlin()
	 */
	@Test
	public void should_not_parse_SourceDebugExtension_attribute_when_not_Kotlin() {
		analyzer.visitSource("Foo.kt", "SMAP\n");
		analyzer.visitAnnotation("LAnnotation;", false);
		assertNull(analyzer.getKotlinSMAP());
	}

	/**
	 * When {@code inline} methods are not used in Kotlin class.
	 *
	 * @see #should_not_parse_SourceDebugExtension_attribute_when_not_Kotlin()
	 * @see #should_parse_SourceDebugExtension_attribute_when_Kotlin()
	 */
	@Test
	public void should_not_parse_absent_SourceDebugExtension_attribute_when_kotlin() {
		analyzer.visitSource("Foo.kt", null);
		analyzer.visitAnnotation("Lkotlin/Metadata;", false);
		assertNull(analyzer.getKotlinSMAP());
	}

	/**
	 * @see #should_not_parse_SourceDebugExtension_attribute_when_not_Kotlin()
	 * @see #should_not_parse_absent_SourceDebugExtension_attribute_when_kotlin()
	 */
	@Test
	public void should_parse_SourceDebugExtension_attribute_when_Kotlin() {
		analyzer.visitSource("Example.kt", "SMAP\n" //
				+ "Example.kt\n" // OutputFileName=Example.kt
				+ "Kotlin\n" // DefaultStratumId=Kotlin
				+ "*S Kotlin\n" // StratumID=Kotlin
				+ "*F\n" // FileSection
				+ "+ 1 Example.kt\n" // FileID=1,FileName=Example.kt
				+ "ExampleKt\n" //
				+ "*L\n" // LineSection
				+ "1#1,3:1\n" // InputStartLine=1,LineFileID=1,RepeatCount=3,OutputStartLine=1
				+ "*E\n"); // EndSection
		analyzer.visitAnnotation("Lkotlin/Metadata;", false);
		assertNotNull(analyzer.getKotlinSMAP());
	}

}
