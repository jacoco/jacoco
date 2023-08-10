/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.data;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;

import org.jacoco.core.data.ExecutionDataWriter;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link CRC64}.
 */
public class CRC64Test {

	@Test
	public void except_java_9_checksums_should_be_different_for_different_bytecode_versions() {
		assertEquals(0x589E9080A572741EL,
				CRC64.classId(createClass(Opcodes.V10)));

		// should remove workaround for Java 9
		// during change of exec file version
		assertEquals(0x1007, ExecutionDataWriter.FORMAT_VERSION);

		assertEquals(0xB5284860A572741CL,
				CRC64.classId(createClass(Opcodes.V9)));

		assertEquals(0xB5284860A572741CL,
				CRC64.classId(createClass(Opcodes.V1_8)));

		assertEquals(0x45284D30A572741AL,
				CRC64.classId(createClass(Opcodes.V1_7)));
	}

	private static byte[] createClass(final int version) {
		final ClassWriter cw = new ClassWriter(0);
		cw.visit(version, 0, "Foo", null, "java/lang/Object", null);
		cw.visitEnd();
		cw.toByteArray();
		return cw.toByteArray();
	}

	@Test
	public void test0() {
		final long sum = CRC64.classId(new byte[0]);
		assertEquals(0L, sum);
	}

	/**
	 * Example taken from http://swissknife.sourceforge.net/docs/CRC64.html
	 *
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void test1() throws UnsupportedEncodingException {
		final long sum = CRC64.classId("IHATEMATH".getBytes("ASCII"));
		assertEquals(0xE3DCADD69B01ADD1L, sum);
	}

	/**
	 * Example generated with http://fsumfe.sourceforge.net/
	 *
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void test2() {
		final long sum = CRC64.classId(
				new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
						(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff });
		assertEquals(0x5300000000000000L, sum);
	}

	/**
	 * Example generated with http://fsumfe.sourceforge.net/
	 *
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void test3() throws UnsupportedEncodingException {
		final long sum = CRC64
				.classId("JACOCO_JACOCO_JACOCO_JACOCO".getBytes("ASCII"));
		assertEquals(0xD8016B38AAD48308L, sum);
	}

}
