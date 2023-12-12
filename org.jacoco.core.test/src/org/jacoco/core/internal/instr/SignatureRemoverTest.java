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
package org.jacoco.core.internal.instr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link SignatureRemover}.
 */
public class SignatureRemoverTest {

	private SignatureRemover remover;

	@Before
	public void setup() {
		remover = new SignatureRemover();
	}

	@Test
	public void testRemoveNegative1() {
		assertFalse(remover.removeEntry("META-INF/ALIAS.MF"));
	}

	@Test
	public void testRemoveNegative2() {
		assertFalse(remover.removeEntry("META-INF/sub/ALIAS.SF"));
	}

	@Test
	public void testRemoveNegative3() {
		remover.setActive(false);
		assertFalse(remover.removeEntry("META-INF/SIG-ALIAS"));
	}

	@Test
	public void testRemovePositive1() {
		assertTrue(remover.removeEntry("META-INF/ALIAS.SF"));
	}

	@Test
	public void testRemovePositive2() {
		assertTrue(remover.removeEntry("META-INF/ALIAS.RSA"));
	}

	@Test
	public void testRemovePositive3() {
		assertTrue(remover.removeEntry("META-INF/ALIAS.DSA"));
	}

	@Test
	public void testRemovePositive4() {
		assertTrue(remover.removeEntry("META-INF/SIG-ALIAS"));
	}

	@Test
	public void testFilterNegative1() throws IOException {
		assertFalse(remover.filterEntry("MANIFEST.MF", null, null));
	}

	@Test
	public void testFilterNegative2() throws IOException {
		remover.setActive(false);
		assertFalse(remover.filterEntry("META-INF/MANIFEST.MF", null, null));
	}

	@Test
	public void testFilterPositive1() throws IOException {
		String original = "Manifest-Version: 1.0\r\n"
				+ "Created-By: Apache Maven\r\n" //
				+ "Bundle-SymbolicName: org.jacoco.core\r\n" //
				+ "\r\n"//
				+ "Name: org/jacoco/example/A.class\r\n" //
				+ "SHA1-Digest: z1ly8OewPb9LOCpfNaIAhEgXZ5I=\r\n" //
				+ "\r\n" //
				+ "Name: org/jacoco/example/B.class\r\n" //
				+ "SHA1-Digest: nfE4+Vmekj0pE5z0m0frpb10Gl0=\r\n" //
				+ "OtherInfo: keep this\r\n" //
				+ "\r\n" //
				+ "Name: org/jacoco/example/C.class\r\n" //
				+ "SHA1-Digest: xaNEXNWCrlTVcqPrXL0TwTcsvXU=\r\n";
		InputStream in = new ByteArrayInputStream(
				original.getBytes("ISO-8859-1"));
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		assertTrue(remover.filterEntry("META-INF/MANIFEST.MF", in, out));

		String expected = "Manifest-Version: 1.0\r\n"
				+ "Created-By: Apache Maven\r\n" //
				+ "Bundle-SymbolicName: org.jacoco.core\r\n" //
				+ "\r\n"//
				+ "Name: org/jacoco/example/B.class\r\n" //
				+ "OtherInfo: keep this\r\n" //
				+ "\r\n";
		assertEquals(createManifest(expected.getBytes("ISO-8859-1")),
				createManifest(out.toByteArray()));
	}

	private static Manifest createManifest(final byte[] bytes)
			throws IOException {
		return new Manifest(new ByteArrayInputStream(bytes));
	}
}
