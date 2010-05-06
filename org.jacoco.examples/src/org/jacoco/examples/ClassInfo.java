/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.examples;

import java.io.FileInputStream;
import java.io.IOException;

import org.jacoco.core.instr.CRC64;
import org.objectweb.asm.ClassReader;

/**
 * This example reads given Java class files and calculates the CRC64
 * identifiers which are used by JaCoCo.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ClassInfo {

	/**
	 * Reads all class file specified as the arguments and dumps information
	 * about it to <code>stdout</code>.
	 * 
	 * @param args
	 *            list of class files
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {
		for (final String file : args) {
			dumpInfo(file);
		}
	}

	private static void dumpInfo(final String file) throws IOException {
		final FileInputStream in = new FileInputStream(file);
		final ClassReader reader = new ClassReader(in);
		in.close();

		System.out.printf("class file: %s%n", file);
		System.out.printf("class name: %s%n", reader.getClassName());
		final long id = CRC64.checksum(reader.b);
		System.out.printf("class id:   %016x%n%n", Long.valueOf(id));
	}

}
