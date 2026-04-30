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

import java.lang.reflect.Field;

import org.junit.Test;
import org.openjdk.jol.datamodel.Model64;
import org.openjdk.jol.datamodel.Model64_Lilliput;
import org.openjdk.jol.info.ClassData;
import org.openjdk.jol.layouters.CurrentLayouter;
import org.openjdk.jol.layouters.HotSpotLayouter;
import org.openjdk.jol.layouters.Layouter;

/**
 * Test of memory required for {@link LineImpl} instance and its singletons.
 */
public class LineImplMemoryTest {

	/**
	 * TODO add comment
	 */
	@Test
	public void current() throws Exception {
		final Layouter layouter = new CurrentLayouter();
		assertEquals(text(
				"org.jacoco.core.internal.analysis.LineImpl object internals:",
				"OFF  SZ                                            TYPE DESCRIPTION               VALUE",
				"  0   8                                                 (object header: mark)     N/A",
				"  8   4                                                 (object header: class)    N/A",
				" 12   4   org.jacoco.core.internal.analysis.CounterImpl LineImpl.instructions     N/A",
				" 16   4   org.jacoco.core.internal.analysis.CounterImpl LineImpl.branches         N/A",
				" 20   4                                                 (object alignment gap)    ",
				"Instance size: 24 bytes",
				"Space losses: 0 bytes internal + 4 bytes external = 4 bytes total"),
				layouter.layout(ClassData.parseClass(LineImpl.class))
						.toPrintable());
		assertEquals(68600, sizeOfSingletons(layouter));
	}

	/**
	 * JDK 24 and above with {@code -XX:+UseCompactObjectHeaders}.
	 *
	 * @see <a href="https://openjdk.org/jeps/450">JEP 450: Compact Object
	 *      Headers (Experimental)</a> delivered in JDK 24
	 * @see <a href="https://openjdk.org/jeps/519">JEP 519: Compact Object
	 *      Headers</a> delivered in JDK 25
	 * @see <a href="https://openjdk.org/jeps/534">JEP 534: Compact Object
	 *      Headers by Default</a>
	 */
	@Test
	public void compact_object_headers() throws Exception {
		final Layouter layouter = new HotSpotLayouter(new Model64_Lilliput(true,
				8, /*
					 * Lilliput 2 (4-byte headers)
					 * https://openjdk.org/jeps/8349069
					 */ false), 24);
		assertEquals(text(
				"org.jacoco.core.internal.analysis.LineImpl object internals:",
				"OFF  SZ                                            TYPE DESCRIPTION               VALUE",
				"  0   8                                                 (object header: mark)     N/A",
				"  8   4   org.jacoco.core.internal.analysis.CounterImpl LineImpl.instructions     N/A",
				" 12   4   org.jacoco.core.internal.analysis.CounterImpl LineImpl.branches         N/A",
				"Instance size: 16 bytes",
				"Space losses: 0 bytes internal + 0 bytes external = 0 bytes total"),
				layouter.layout(ClassData.parseClass(LineImpl.class))
						.toPrintable());
		assertEquals(48432, sizeOfSingletons(layouter));
	}

	/**
	 * JDK 24 and above with {@code -XX:+UseCompactObjectHeaders -Xmx32g}.
	 */
	@Test
	public void compact_object_headers_without_compressed_references()
			throws Exception {
		final Layouter layouter = new HotSpotLayouter(new Model64_Lilliput(),
				24);
		assertEquals(text(
				"org.jacoco.core.internal.analysis.LineImpl object internals:",
				"OFF  SZ                                            TYPE DESCRIPTION               VALUE",
				"  0   8                                                 (object header: mark)     N/A",
				"  8   8   org.jacoco.core.internal.analysis.CounterImpl LineImpl.instructions     N/A",
				" 16   8   org.jacoco.core.internal.analysis.CounterImpl LineImpl.branches         N/A",
				"Instance size: 24 bytes",
				"Space losses: 0 bytes internal + 0 bytes external = 0 bytes total"),
				layouter.layout(ClassData.parseClass(LineImpl.class))
						.toPrintable());
		assertEquals(76696, sizeOfSingletons(layouter));
	}

	/**
	 * JDK 15 and above with {@code -Xmx32g}.
	 * https://bugs.openjdk.org/browse/JDK-8241825
	 */
	@Test
	public void compressed_class_pointers() throws Exception {
		final Layouter layouter = new HotSpotLayouter(new Model64(false, true),
				15);
		assertEquals(text(
				"org.jacoco.core.internal.analysis.LineImpl object internals:",
				"OFF  SZ                                            TYPE DESCRIPTION               VALUE",
				"  0   8                                                 (object header: mark)     N/A",
				"  8   4                                                 (object header: class)    N/A",
				" 12   4                                                 (alignment/padding gap)   ",
				" 16   8   org.jacoco.core.internal.analysis.CounterImpl LineImpl.instructions     N/A",
				" 24   8   org.jacoco.core.internal.analysis.CounterImpl LineImpl.branches         N/A",
				"Instance size: 32 bytes",
				"Space losses: 4 bytes internal + 0 bytes external = 4 bytes total"),
				layouter.layout(ClassData.parseClass(LineImpl.class))
						.toPrintable());
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
				"org.jacoco.core.internal.analysis.LineImpl object internals:",
				"OFF  SZ                                            TYPE DESCRIPTION               VALUE",
				"  0   8                                                 (object header: mark)     N/A",
				"  8   8                                                 (object header: class)    N/A",
				" 16   8   org.jacoco.core.internal.analysis.CounterImpl LineImpl.instructions     N/A",
				" 24   8   org.jacoco.core.internal.analysis.CounterImpl LineImpl.branches         N/A",
				"Instance size: 32 bytes",
				"Space losses: 0 bytes internal + 0 bytes external = 0 bytes total"),
				layouter.layout(ClassData.parseClass(LineImpl.class))
						.toPrintable());
		assertEquals(96864, sizeOfSingletons(layouter));
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
