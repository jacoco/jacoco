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
package org.jacoco.core.internal.analysis;

import static org.junit.Assert.assertEquals;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;

import org.jacoco.core.test.validation.JavaVersion;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.openjdk.jol.datamodel.Model64;
import org.openjdk.jol.datamodel.Model64_Lilliput;
import org.openjdk.jol.info.ClassData;
import org.openjdk.jol.layouters.CurrentLayouter;
import org.openjdk.jol.layouters.HotSpotLayouter;
import org.openjdk.jol.layouters.Layouter;

/**
 * Test of memory required for {@link LineImpl} instance and its singletons.
 *
 * Goal of simulations with {@link HotSpotLayouter} such as
 * {@link #compact_object_headers()} is to catch changes in JaCoCo for already
 * explored VM changes in known configurations.
 *
 * These simulations might miss changes in VM unknown to JOL version in use, but
 * hopefully such will be caught by {@link #currentVM() test} with
 * {@link CurrentLayouter} - see {@link #clusterOops()} for examples of such
 * changes.
 *
 * @see <a href="https://shipilev.net/jvm/objects-inside-out/">Java Objects
 *      Inside Out</a>
 */
public class LineImplMemoryTest {

	/**
	 * Current VM. Intentionally - to be able to catch changes in VM when heap
	 * size is below 32 GB.
	 */
	@Test
	public void currentVM() throws Exception {
		final Layouter layouter = currentLayouter();
		if (JavaVersion.current().isBefore("27")) {
			assertEquals(text( //
					"Current VM Layout",
					"org.jacoco.core.internal.analysis.LineImpl object internals:",
					"OFF  SZ                                            TYPE DESCRIPTION               VALUE",
					"  0   8                                                 (object header: mark)     N/A",
					"  8   4                                                 (object header: class)    N/A",
					" 12   4   org.jacoco.core.internal.analysis.CounterImpl LineImpl.instructions     N/A",
					" 16   4   org.jacoco.core.internal.analysis.CounterImpl LineImpl.branches         N/A",
					" 20   4                                                 (object alignment gap)    ",
					"Instance size: 24 bytes",
					"Space losses: 0 bytes internal + 4 bytes external = 4 bytes total"),
					layout(layouter));
			assertEquals(68600, sizeOfSingletons(layouter));
		} else {
			// https://openjdk.org/jeps/534
			assertEquals(text( //
					"Current VM Layout",
					"org.jacoco.core.internal.analysis.LineImpl object internals:",
					"OFF  SZ                                            TYPE DESCRIPTION               VALUE",
					"  0   8                                                 (object header: mark)     N/A",
					"  8   4   org.jacoco.core.internal.analysis.CounterImpl LineImpl.instructions     N/A",
					" 12   4   org.jacoco.core.internal.analysis.CounterImpl LineImpl.branches         N/A",
					"Instance size: 16 bytes",
					"Space losses: 0 bytes internal + 0 bytes external = 0 bytes total"),
					layout(layouter));
			assertEquals(48432, sizeOfSingletons(layouter));
		}
	}

	/**
	 * Potentially experimental in JDK 30 and default in JDK 33
	 * https://bugs.openjdk.org/browse/JDK-8360700?focusedId=14862778&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-14862778
	 */
	@Test
	public void lilliput2() throws Exception {
		final Layouter layouter = new HotSpotLayouter(
				new Model64_Lilliput(/* compressed references */ true, 8,
						/* Lilliput 2 */ true),
				30);
		assertEquals(text(
				"Hotspot Layout Simulation (JDK 30, 64-bit model, Lilliput (ultimate target), compressed references, compressed classes, 8-byte aligned)",
				"org.jacoco.core.internal.analysis.LineImpl object internals:",
				"OFF  SZ                                            TYPE DESCRIPTION               VALUE",
				"  0   1                                                 (object header: mark)     N/A",
				"  1   3                                                 (object header: class)    N/A",
				"  4   4   org.jacoco.core.internal.analysis.CounterImpl LineImpl.instructions     N/A",
				"  8   4   org.jacoco.core.internal.analysis.CounterImpl LineImpl.branches         N/A",
				" 12   4                                                 (object alignment gap)    ",
				"Instance size: 16 bytes",
				"Space losses: 0 bytes internal + 4 bytes external = 4 bytes total"),
				layout(layouter));
		assertEquals(48432, sizeOfSingletons(layouter));
	}

	@Test
	public void lilliput2_without_compressed_references() throws Exception {
		final Layouter layouter = new HotSpotLayouter(
				new Model64_Lilliput(/* compressed references */ false, 8,
						/* Lilliput 2 */ true),
				30);
		assertEquals(text(
				"Hotspot Layout Simulation (JDK 30, 64-bit model, Lilliput (ultimate target), NO compressed references, compressed classes, 8-byte aligned)",
				"org.jacoco.core.internal.analysis.LineImpl object internals:",
				"OFF  SZ                                            TYPE DESCRIPTION               VALUE",
				"  0   1                                                 (object header: mark)     N/A",
				"  1   3                                                 (object header: class)    N/A",
				"  4   4                                                 (alignment/padding gap)   ",
				"  8   8   org.jacoco.core.internal.analysis.CounterImpl LineImpl.instructions     N/A",
				" 16   8   org.jacoco.core.internal.analysis.CounterImpl LineImpl.branches         N/A",
				"Instance size: 24 bytes",
				"Space losses: 4 bytes internal + 0 bytes external = 4 bytes total"),
				layout(layouter));
		assertEquals(72728, sizeOfSingletons(layouter));
	}

	/**
	 * <ul>
	 * <li>JDK 24, 25 and 26 with {@code -XX:+UseCompactObjectHeaders}</li>
	 * <li>JDK 27 and above</li>
	 * </ul>
	 *
	 * @see <a href="https://openjdk.org/jeps/450">JEP 450: Compact Object
	 *      Headers (Experimental)</a> delivered in JDK 24
	 * @see <a href="https://openjdk.org/jeps/519">JEP 519: Compact Object
	 *      Headers</a> delivered in JDK 25
	 * @see <a href="https://openjdk.org/jeps/534">JEP 534: Compact Object
	 *      Headers by Default</a> delivered in JDK 27
	 */
	@Test
	public void compact_object_headers() throws Exception {
		final Layouter layouter = new HotSpotLayouter(
				new Model64_Lilliput(true, 8, //
						/*
						 * Not Lilliput 2 (4-byte headers)
						 * https://openjdk.org/jeps/8349069
						 */
						false),
				24);
		assertEquals(text(
				"Hotspot Layout Simulation (JDK 24, 64-bit model, Lilliput (current experiment), compressed references, compressed classes, 8-byte aligned)",
				"org.jacoco.core.internal.analysis.LineImpl object internals:",
				"OFF  SZ                                            TYPE DESCRIPTION               VALUE",
				"  0   8                                                 (object header: mark)     N/A",
				"  8   4   org.jacoco.core.internal.analysis.CounterImpl LineImpl.instructions     N/A",
				" 12   4   org.jacoco.core.internal.analysis.CounterImpl LineImpl.branches         N/A",
				"Instance size: 16 bytes",
				"Space losses: 0 bytes internal + 0 bytes external = 0 bytes total"),
				layout(layouter));
		assertEquals(48432, sizeOfSingletons(layouter));
	}

	/**
	 * <ul>
	 * <li>JDK 24, 25 and 26 with
	 * {@code -XX:+UseCompactObjectHeaders -Xmx32g}</li>
	 * <li>JDK 27 and above with {@code -Xmx32g}</li>
	 * </ul>
	 */
	@Test
	public void compact_object_headers_without_compressed_references()
			throws Exception {
		final Layouter layouter = new HotSpotLayouter(new Model64_Lilliput(),
				24);
		assertEquals(text(
				"Hotspot Layout Simulation (JDK 24, 64-bit model, Lilliput (current experiment), NO compressed references, compressed classes, 8-byte aligned)",
				"org.jacoco.core.internal.analysis.LineImpl object internals:",
				"OFF  SZ                                            TYPE DESCRIPTION               VALUE",
				"  0   8                                                 (object header: mark)     N/A",
				"  8   8   org.jacoco.core.internal.analysis.CounterImpl LineImpl.instructions     N/A",
				" 16   8   org.jacoco.core.internal.analysis.CounterImpl LineImpl.branches         N/A",
				"Instance size: 24 bytes",
				"Space losses: 0 bytes internal + 0 bytes external = 0 bytes total"),
				layout(layouter));
		assertEquals(76696, sizeOfSingletons(layouter));
	}

	/**
	 * JDK 15 and above up to JDK 26 with {@code -Xmx32g}.
	 *
	 * @see <a href= "https://bugs.openjdk.org/browse/JDK-8241825">JDK-8241825:
	 *      Make compressed oops and compressed class pointers independent</a>
	 */
	@Test
	public void compressed_class_pointers_without_compressed_references()
			throws Exception {
		final Layouter layouter = new HotSpotLayouter(new Model64(false, true),
				15);
		assertEquals(text(
				"Hotspot Layout Simulation (JDK 15, 64-bit model, NO compressed references, compressed classes, 8-byte aligned)",
				"org.jacoco.core.internal.analysis.LineImpl object internals:",
				"OFF  SZ                                            TYPE DESCRIPTION               VALUE",
				"  0   8                                                 (object header: mark)     N/A",
				"  8   4                                                 (object header: class)    N/A",
				" 12   4                                                 (alignment/padding gap)   ",
				" 16   8   org.jacoco.core.internal.analysis.CounterImpl LineImpl.instructions     N/A",
				" 24   8   org.jacoco.core.internal.analysis.CounterImpl LineImpl.branches         N/A",
				"Instance size: 32 bytes",
				"Space losses: 4 bytes internal + 0 bytes external = 4 bytes total"),
				layout(layouter));
		assertEquals(92896, sizeOfSingletons(layouter));
	}

	/**
	 * Prior to JDK 15 with {@code -Xmx32g}.
	 */
	@Test
	public void without_compressed_class_pointers_and_without_compressed_references()
			throws Exception {
		final Layouter layouter = new HotSpotLayouter(new Model64(false, false),
				8);
		assertEquals(text(
				"Hotspot Layout Simulation (JDK 8, 64-bit model, NO compressed references, NO compressed classes, 8-byte aligned)",
				"org.jacoco.core.internal.analysis.LineImpl object internals:",
				"OFF  SZ                                            TYPE DESCRIPTION               VALUE",
				"  0   8                                                 (object header: mark)     N/A",
				"  8   8                                                 (object header: class)    N/A",
				" 16   8   org.jacoco.core.internal.analysis.CounterImpl LineImpl.instructions     N/A",
				" 24   8   org.jacoco.core.internal.analysis.CounterImpl LineImpl.branches         N/A",
				"Instance size: 32 bytes",
				"Space losses: 0 bytes internal + 0 bytes external = 0 bytes total"),
				layout(layouter));
		assertEquals(96864, sizeOfSingletons(layouter));
	}

	private static String layout(final Layouter layouter) {
		return layouter + "\n" //
				+ layouter.layout(ClassData.parseClass(LineImpl.class))
						.toPrintable();
	}

	private static long sizeOfSingletons(final Layouter layouter)
			throws NoSuchFieldException, IllegalAccessException {
		final Field field = LineImpl.class.getDeclaredField("SINGLETONS");
		field.setAccessible(true);
		final LineImpl[][][][] singletons = (LineImpl[][][][]) field.get(null);
		int instances = 0;
		long size = sizeOf(singletons, layouter);
		for (int i = 0; i < singletons.length; i++) {
			size += sizeOf(singletons[i], layouter);
			for (int j = 0; j < singletons[i].length; j++) {
				size += sizeOf(singletons[i][j], layouter);
				for (int k = 0; k < singletons[i][j].length; k++) {
					size += sizeOf(singletons[i][j][k], layouter);
					for (int l = 0; l < singletons[i][j][k].length; l++) {
						instances++;
						size += sizeOf(singletons[i][j][k][l], layouter);
					}
				}
			}
		}
		assertEquals("instances", 2025, instances);
		return size;
	}

	private static long sizeOf(final Object instance, final Layouter layouter) {
		return layouter.layout(ClassData.parseInstance(instance))
				.instanceSize();
	}

	private static CurrentLayouter currentLayouter() {
		try {
			ManagementFactory.getOperatingSystemMXBean();
		} catch (final NullPointerException e) {
			// Frequently happens in CI with JDK version 18 due to
			// https://bugs.openjdk.java.net/browse/JDK-8287073
			// preventing use of
			// "com.sun.management:type=HotSpotDiagnostic" MXBean
			// in org.openjdk.jol.vm.VMOptions
			// to obtain "ObjectAlignmentInBytes"
			if (JavaVersion.current().isBefore("19")
					&& !JavaVersion.current().isBefore("18")) {
				throw new AssumptionViolatedException(
						"this test requires HotSpotDiagnosticMXBean");
			}
			throw e;
		}
		if ("32".equals(System.getProperty("sun.arch.data.model"))) {
			throw new AssumptionViolatedException(
					"this test does not support 32-bit architecture");
		}
		return new CurrentLayouter();
	}

	/**
	 * @see <a href="https://bugs.openjdk.org/browse/JDK-8353273">JDK-8353273:
	 *      Reduce number of oop map entries in instances</a> in JDK 25 and
	 *      <a href=
	 *      "https://github.com/openjdk/jol/commit/acb7bf9480bfd0588e93b353aa7217d3fe3efabe">corresponding
	 *      change in JOL</a>
	 * @see <a href="https://bugs.openjdk.org/browse/JDK-8139457">JDK-8139457:
	 *      Relax alignment of array elements</a> in JDK 23 and <a href=
	 *      "https://github.com/openjdk/jol/commit/8c4d7be996489676b9ff9caef83610b15c726019">corresponding
	 *      change in JOL</a> as another example of change in VM unknown to JOL
	 *      {@link HotSpotLayouter} simulation in version 0.17
	 */
	@Test
	public void clusterOops() {
		final Layouter layouter = currentLayouter();
		if (JavaVersion.current().isBefore("25")) {
			assertEquals(text(
					"org.jacoco.core.internal.analysis.LineImplMemoryTest$Derived object internals:",
					"OFF  SZ               TYPE DESCRIPTION               VALUE",
					"  0   8                    (object header: mark)     N/A",
					"  8   4                    (object header: class)    N/A",
					" 12   4                int Base.nonOop               N/A",
					" 16   4   java.lang.Object Base.oop                  N/A",
					" 20   4                int Derived.nonOop            N/A",
					" 24   4   java.lang.Object Derived.oop               N/A",
					" 28   4                    (object alignment gap)    ",
					"Instance size: 32 bytes",
					"Space losses: 0 bytes internal + 4 bytes external = 4 bytes total"),
					layouter.layout(ClassData.parseClass(Derived.class))
							.toPrintable());
		} else if (JavaVersion.current().isBefore("27")) {
			assertEquals(text(
					"org.jacoco.core.internal.analysis.LineImplMemoryTest$Derived object internals:",
					"OFF  SZ               TYPE DESCRIPTION               VALUE",
					"  0   8                    (object header: mark)     N/A",
					"  8   4                    (object header: class)    N/A",
					" 12   4                int Base.nonOop               N/A",
					" 16   4   java.lang.Object Base.oop                  N/A",
					" 20   4   java.lang.Object Derived.oop               N/A",
					" 24   4                int Derived.nonOop            N/A",
					" 28   4                    (object alignment gap)    ",
					"Instance size: 32 bytes",
					"Space losses: 0 bytes internal + 4 bytes external = 4 bytes total"),
					layouter.layout(ClassData.parseClass(Derived.class))
							.toPrintable());
		} else {
			// https://openjdk.org/jeps/534
			assertEquals(text(
					"org.jacoco.core.internal.analysis.LineImplMemoryTest$Derived object internals:",
					"OFF  SZ               TYPE DESCRIPTION               VALUE",
					"  0   8                    (object header: mark)     N/A",
					"  8   4                int Base.nonOop               N/A",
					" 12   4   java.lang.Object Base.oop                  N/A",
					" 16   4   java.lang.Object Derived.oop               N/A",
					" 20   4                int Derived.nonOop            N/A",
					"Instance size: 24 bytes",
					"Space losses: 0 bytes internal + 0 bytes external = 0 bytes total"),
					layouter.layout(ClassData.parseClass(Derived.class))
							.toPrintable());
		}
	}

	private static class Base {
		private Object oop;
		private int nonOop;
	}

	private static class Derived extends Base {
		private Object oop;
		private int nonOop;
	}

	/**
	 * Poor man's replacement for <a href="https://openjdk.org/jeps/378">Java 15
	 * Text Blocks</a>.
	 */
	private static String text(String... text) {
		final StringBuilder sb = new StringBuilder();
		for (String line : text) {
			sb.append(line).append('\n');
		}
		return sb.toString();
	}

}
