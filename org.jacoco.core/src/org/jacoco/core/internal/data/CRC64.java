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

import org.objectweb.asm.Opcodes;

/**
 * CRC64 checksum calculator based on the polynom specified in ISO 3309. The
 * implementation is based on the following publications:
 *
 * <ul>
 * <li>http://en.wikipedia.org/wiki/Cyclic_redundancy_check</li>
 * <li>http://www.geocities.com/SiliconValley/Pines/8659/crc.htm</li>
 * </ul>
 */
public final class CRC64 {

	private static final long POLY64REV = 0xd800000000000000L;

	private static final long[] LOOKUPTABLE;

	static {
		LOOKUPTABLE = new long[0x100];
		for (int i = 0; i < 0x100; i++) {
			long v = i;
			for (int j = 0; j < 8; j++) {
				if ((v & 1) == 1) {
					v = (v >>> 1) ^ POLY64REV;
				} else {
					v = (v >>> 1);
				}
			}
			LOOKUPTABLE[i] = v;
		}
	}

	/**
	 * Updates given checksum by given byte.
	 *
	 * @param sum
	 *            initial checksum value
	 * @param b
	 *            byte to update the checksum with
	 * @return updated checksum value
	 */
	private static long update(final long sum, final byte b) {
		final int lookupidx = ((int) sum ^ b) & 0xff;
		return (sum >>> 8) ^ LOOKUPTABLE[lookupidx];
	}

	/**
	 * Updates given checksum by bytes from given array.
	 *
	 * @param sum
	 *            initial checksum value
	 * @param bytes
	 *            byte array to update the checksum with
	 * @param fromIndexInclusive
	 *            start index in array, inclusive
	 * @param toIndexExclusive
	 *            end index in array, exclusive
	 * @return updated checksum value
	 */
	private static long update(long sum, final byte[] bytes,
			final int fromIndexInclusive, final int toIndexExclusive) {
		for (int i = fromIndexInclusive; i < toIndexExclusive; i++) {
			sum = update(sum, bytes[i]);
		}
		return sum;
	}

	/**
	 * Calculates class identifier for the given class bytes.
	 *
	 * @param bytes
	 *            class bytes
	 * @return class identifier
	 */
	public static long classId(final byte[] bytes) {
		if (bytes.length > 7 && bytes[6] == 0x00 && bytes[7] == Opcodes.V9) {
			// To support early versions of Java 9 we did a trick - change of
			// Java 9 class files version on Java 8. Unfortunately this also
			// affected class identifiers.
			long sum = update(0, bytes, 0, 7);
			sum = update(sum, (byte) Opcodes.V1_8);
			return update(sum, bytes, 8, bytes.length);
		}
		return update(0, bytes, 0, bytes.length);
	}

	private CRC64() {
	}

}
