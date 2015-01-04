/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * Test utility to remove debug information from class files.
 */
public class RemoveDebugInfos {

	public static void main(String[] args) throws Exception {
		final InputStream in = new FileInputStream(args[0]);
		final ClassReader reader = new ClassReader(in);
		in.close();

		final ClassWriter writer = new ClassWriter(0);
		reader.accept(writer, ClassReader.SKIP_DEBUG);

		final OutputStream out = new FileOutputStream(args[1]);
		out.write(writer.toByteArray());
		out.close();
	}

}
