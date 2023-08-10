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
package org.jacoco.ant;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.jacoco.core.internal.InputStreams;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * Test utility to remove debug information from class files.
 */
public class RemoveDebugInfos {

	public static void main(String[] args) throws Exception {
		final InputStream in = new FileInputStream(args[0]);
		final ClassReader reader = InstrSupport
				.classReaderFor(InputStreams.readFully(in));
		in.close();

		final ClassWriter writer = new ClassWriter(0);
		reader.accept(writer, ClassReader.SKIP_DEBUG);

		final OutputStream out = new FileOutputStream(args[1]);
		out.write(writer.toByteArray());
		out.close();
	}

}
