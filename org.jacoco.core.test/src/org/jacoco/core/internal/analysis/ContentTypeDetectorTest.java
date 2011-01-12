/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.analysis;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jacoco.core.internal.analysis.ContentTypeDetector;
import org.jacoco.core.test.TargetLoader;
import org.junit.Test;

/**
 * Unit tests for {@link ContentTypeDetector}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class ContentTypeDetectorTest {

	private byte[] data;

	private ContentTypeDetector detector;

	@Test
	public void testEmptyStream() throws IOException {
		initData();
		assertEquals(ContentTypeDetector.UNKNOWN, detector.getType());
		assertContent();
	}

	@Test
	public void testClassFile() throws IOException {
		initData(TargetLoader
				.getClassDataAsBytes(ContentTypeDetectorTest.class));
		assertEquals(ContentTypeDetector.CLASSFILE, detector.getType());
		assertContent();
	}

	@Test
	public void testClassFile11() throws IOException {
		initData(0xCA, 0xFE, 0xBA, 0xBE, 0x00, 0x03, 0x00, 0x2D);
		assertEquals(ContentTypeDetector.CLASSFILE, detector.getType());
		assertContent();
	}

	@Test
	public void testClassFile12() throws IOException {
		initData(0xCA, 0xFE, 0xBA, 0xBE, 0x00, 0x00, 0x00, 0x2E);
		assertEquals(ContentTypeDetector.CLASSFILE, detector.getType());
		assertContent();
	}

	@Test
	public void testClassFile13() throws IOException {
		initData(0xCA, 0xFE, 0xBA, 0xBE, 0x00, 0x00, 0x00, 0x2F);
		assertEquals(ContentTypeDetector.CLASSFILE, detector.getType());
		assertContent();
	}

	@Test
	public void testClassFile14() throws IOException {
		initData(0xCA, 0xFE, 0xBA, 0xBE, 0x00, 0x00, 0x00, 0x30);
		assertEquals(ContentTypeDetector.CLASSFILE, detector.getType());
		assertContent();
	}

	@Test
	public void testClassFile15() throws IOException {
		initData(0xCA, 0xFE, 0xBA, 0xBE, 0x00, 0x00, 0x00, 0x31);
		assertEquals(ContentTypeDetector.CLASSFILE, detector.getType());
		assertContent();
	}

	@Test
	public void testClassFile16() throws IOException {
		initData(0xCA, 0xFE, 0xBA, 0xBE, 0x00, 0x00, 0x00, 0x32);
		assertEquals(ContentTypeDetector.CLASSFILE, detector.getType());
		assertContent();
	}

	@Test
	public void testClassFile17() throws IOException {
		initData(0xCA, 0xFE, 0xBA, 0xBE, 0x00, 0x00, 0x00, 0x33);
		assertEquals(ContentTypeDetector.CLASSFILE, detector.getType());
		assertContent();
	}

	@Test
	public void testMachObjectFile() throws IOException {
		initData(0xCA, 0xFE, 0xBA, 0xBE, 0x00, 0x00, 0x00, 0x02);
		assertEquals(ContentTypeDetector.UNKNOWN, detector.getType());
		assertContent();
	}

	@Test
	public void testZipFile() throws IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final ZipOutputStream zip = new ZipOutputStream(buffer);
		zip.putNextEntry(new ZipEntry("hello.txt"));
		zip.write("Hello Zip!".getBytes());
		zip.close();
		initData(buffer.toByteArray());
		assertEquals(ContentTypeDetector.ZIPFILE, detector.getType());
		assertContent();
	}

	@Test
	public void testStreamWithoutMarkSupport() throws IOException {
		initData(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07);
		detector = new ContentTypeDetector(new ByteArrayInputStream(data) {

			@Override
			public void mark(int readlimit) {
			}

			@Override
			public void reset() {
			}

			@Override
			public boolean markSupported() {
				return false;
			}

		});
		assertContent();
	}

	private void initData(byte[] bytes) throws IOException {
		this.data = bytes;
		this.detector = new ContentTypeDetector(new ByteArrayInputStream(data));
	}

	private void initData(final int... bytes) throws IOException {
		byte[] data = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			data[i] = (byte) bytes[i];
		}
		initData(data);
	}

	private void assertContent() throws IOException {
		final InputStream actual = detector.getInputStream();
		for (int b : data) {
			assertEquals(b, (byte) actual.read());
		}
		assertEquals(-1, actual.read());
	}

}
