/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.cli.internal.commands;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.jacoco.cli.internal.CommandTestBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link Instrument}.
 */
public class InstrumentTest extends CommandTestBase {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void should_print_usage_when_no_options_are_given()
			throws Exception {
		execute("instrument");
		assertFailure();
		assertContains("\"--dest\"", err);
		assertContains(
				"Usage: java -jar jacococli.jar instrument [<sourcefiles> ...]",
				err);
	}

	@Test
	public void should_instrument_class_files_and_copy_resources_when_folder_is_given()
			throws Exception {
		File destdir = tmp.getRoot();

		execute("instrument", "--dest", destdir.getAbsolutePath(),
				getClassPath());

		assertOk();
		assertContains("[INFO] 14 classes instrumented to "
				+ destdir.getAbsolutePath(), out);

		// non class-file resources are copied:
		assertTrue(new File(destdir, "about.html").isFile());

		assertInstrumented(new File(destdir,
				"org/jacoco/cli/internal/commands/InstrumentTest.class"));
	}

	@Test
	public void should_instrument_class_files_to_dest_folder_when_class_files_are_given()
			throws Exception {
		File destdir = tmp.getRoot();

		File src = new File(getClassPath(),
				"org/jacoco/cli/internal/commands/InstrumentTest.class");

		execute("instrument", "--dest", destdir.getAbsolutePath(),
				src.getAbsolutePath());

		assertOk();
		assertContains(
				"[INFO] 1 classes instrumented to " + destdir.getAbsolutePath(),
				out);

		assertInstrumented(new File(destdir, "InstrumentTest.class"));
	}

	@Test
	public void should_not_instrument_anything_when_no_source_is_given()
			throws Exception {
		File destdir = tmp.getRoot();

		execute("instrument", "--dest", destdir.getAbsolutePath());

		assertOk();
		assertArrayEquals(new String[0], destdir.list());
	}

	@Test
	public void should_not_create_dest_file_when_source_class_is_broken()
			throws Exception {
		File srcdir = new File(tmp.getRoot(), "src");
		srcdir.mkdir();
		File destdir = new File(tmp.getRoot(), "dest");
		destdir.mkdir();

		OutputStream out = new FileOutputStream(
				new File(srcdir, "Broken.class"));
		out.write((byte) 0xca);
		out.write((byte) 0xfe);
		out.write((byte) 0xba);
		out.write((byte) 0xbe);
		out.write((byte) 0x00);
		out.write((byte) 0x00);
		out.write((byte) 0x00);
		out.write((byte) 50);
		out.close();

		try {
			execute("instrument", "--dest", destdir.getAbsolutePath(),
					srcdir.getAbsolutePath());
			fail("exception expected");
		} catch (IOException expected) {
		}

		assertFalse(new File(destdir, "Broken.class").exists());
	}

	private void assertInstrumented(File classfile) throws IOException {
		InputStream in = new FileInputStream(classfile);
		ClassReader reader = new ClassReader(in);
		in.close();
		final Set<String> fields = new HashSet<String>();
		reader.accept(new ClassVisitor(Opcodes.ASM6) {
			@Override
			public FieldVisitor visitField(int access, String name, String desc,
					String signature, Object value) {
				fields.add(name);
				return null;
			}
		}, 0);
		assertTrue(fields.contains("$jacocoData"));
	}

}
