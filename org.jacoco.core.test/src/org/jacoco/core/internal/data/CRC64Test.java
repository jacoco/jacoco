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
package org.jacoco.core.internal.data;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

/**
 * Unit tests for {@link CRC64}.
 */
public class CRC64Test {

	@Test
	public void test0() throws UnsupportedEncodingException {
		final long sum = CRC64.checksum(new byte[0]);
		assertTrue(0L == sum);
	}

	/**
	 * Example taken from http://swissknife.sourceforge.net/docs/CRC64.html
	 * 
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void test1() throws UnsupportedEncodingException {
		final long sum = CRC64.checksum("IHATEMATH".getBytes("ASCII"));
		assertTrue(0xE3DCADD69B01ADD1L == sum);
	}

	/**
	 * Example generated with http://fsumfe.sourceforge.net/
	 * 
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void test2() throws UnsupportedEncodingException {
		final long sum = CRC64.checksum(new byte[] { (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff });
		assertTrue(0x5300000000000000L == sum);
	}

	/**
	 * Example generated with http://fsumfe.sourceforge.net/
	 * 
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void test3() throws UnsupportedEncodingException {
		final long sum = CRC64.checksum("JACOCO_JACOCO_JACOCO_JACOCO"
				.getBytes("ASCII"));
		assertTrue(0xD8016B38AAD48308L == sum);
	}

}
