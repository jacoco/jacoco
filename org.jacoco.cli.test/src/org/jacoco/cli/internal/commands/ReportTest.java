/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    r3bb1t - tests for duplicate classes
 *
 *******************************************************************************/
package org.jacoco.cli.internal.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jacoco.cli.internal.CommandTestBase;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.internal.InputStreams;
import org.jacoco.core.internal.data.CRC64;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link Report}.
 */
public class ReportTest extends CommandTestBase {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void should_print_usage_when_no_options_are_given()
			throws Exception {
		execute("report");

		assertFailure();
		assertContains("\"--classfiles\"", err);
		assertContains(
				"Usage: java -jar jacococli.jar report [<execfiles> ...]", err);
	}

	@Test
	public void should_print_warning_when_no_exec_files_are_provided()
			throws Exception {
		execute("report", "--classfiles", getClassPath());

		assertOk();
		assertContains("[WARN] No execution data files provided.", out);
	}

	@Test
	public void should_print_number_of_analyzed_classes() throws Exception {
		execute("report", "--classfiles", getClassPath());

		assertOk();
		assertContains("[INFO] Analyzing 15 classes.", out);
	}

	@Test
	public void should_print_warning_when_exec_data_does_not_match()
			throws Exception {
		File exec = new File(tmp.getRoot(), "jacoco.exec");
		final FileOutputStream execout = new FileOutputStream(exec);
		ExecutionDataWriter writer = new ExecutionDataWriter(execout);
		// Add probably invalid id for this test class:
		writer.visitClassExecution(
				new ExecutionData(0x123, getClass().getName().replace('.', '/'),
						new boolean[] { true }));
		execout.close();

		execute("report", exec.getAbsolutePath(), "--classfiles",
				getClassPath());

		assertOk();
		assertContains("[WARN] Some classes do not match with execution data.",
				out);
		assertContains(
				"[WARN] For report generation the same class files must be used as at runtime.",
				out);
		assertContains(
				"[WARN] Execution data for class org/jacoco/cli/internal/commands/ReportTest does not match.",
				out);
	}

	@Test
	public void should_create_xml_report_when_xml_option_is_provided()
			throws Exception {
		File xml = new File(tmp.getRoot(), "coverage.xml");

		execute("report", "--classfiles", getClassPath(), "--xml",
				xml.getAbsolutePath());

		assertOk();
		assertTrue(xml.isFile());
	}

	@Test
	public void should_create_csv_report_when_csv_option_is_provided()
			throws Exception {
		File csv = new File(tmp.getRoot(), "coverage.csv");

		execute("report", "--classfiles", getClassPath(), "--csv",
				csv.getAbsolutePath());

		assertOk();
		assertTrue(csv.isFile());
	}

	@Test
	public void should_create_html_report_when_html_option_is_provided()
			throws Exception {
		File html = new File(tmp.getRoot(), "coverage");

		execute("report", "--classfiles", getClassPath(), "--sourcefiles",
				"./src", "--html", html.getAbsolutePath());

		assertOk();
		assertTrue(html.isDirectory());
		assertTrue(new File(html,
				"org.jacoco.cli.internal.commands/ReportTest.html").isFile());
		assertTrue(new File(html,
				"org.jacoco.cli.internal.commands/ReportTest.java.html")
				.isFile());
	}

	@Test
	public void should_use_all_values_when_multiple_classfiles_options_are_provided()
			throws Exception {
		File html = new File(tmp.getRoot(), "coverage");

		final String c1 = getClassPath()
				+ "/org/jacoco/cli/internal/commands/ReportTest.class";
		final String c2 = getClassPath()
				+ "/org/jacoco/cli/internal/commands/DumpTest.class";

		execute("report", "--classfiles", c1, "--classfiles", c2, "--html",
				html.getAbsolutePath());

		assertOk();
		assertTrue(html.isDirectory());
		assertTrue(new File(html,
				"org.jacoco.cli.internal.commands/ReportTest.html").isFile());
		assertTrue(
				new File(html, "org.jacoco.cli.internal.commands/DumpTest.html")
						.isFile());
	}

	@Test
	public void should_fail_on_different_classes_with_same_name_by_default()
			throws Exception {
		final File dir1 = createClassDir("dir1", createClass("a"));
		final File dir2 = createClassDir("dir2", createClass("b"));

		try {
			execute("report", "--classfiles", dir1.getAbsolutePath(),
					"--classfiles", dir2.getAbsolutePath());
			fail("IOException expected");
		} catch (final IOException e) {
			assertEquals("Can't add different class with same name: Foo",
					e.getCause().getMessage());
		}
	}

	@Test
	public void should_report_copy_with_execution_data_when_resolveduplicates_option_is_given()
			throws Exception {
		final byte[] a = createClass("a");
		final byte[] b = createClass("b");
		final File dir1 = createClassDir("dir1", a);
		final File dir2 = createClassDir("dir2", b);
		final File exec = createExecFile(b);
		final File xml = new File(tmp.getRoot(), "coverage.xml");

		execute("report", exec.getAbsolutePath(), "--classfiles",
				dir1.getAbsolutePath(), "--classfiles", dir2.getAbsolutePath(),
				"--resolveduplicates", "--xml", xml.getAbsolutePath());

		assertOk();
		assertContains(
				"[WARN] Some classes were found more than once with different content.",
				out);
		assertContains("[WARN] Class Foo: reporting " + classFile(dir2)
				+ ", ignoring " + classFile(dir1) + ".", out);
		assertContainsNot("does not match", out);
		assertContainsNot("the coverage of the ignored ones is not reported",
				out);
		assertContains("[INFO] Analyzing 1 classes.", out);
		assertReported(xml, "b", "a");
	}

	@Test
	public void should_report_same_copy_whatever_order_classfiles_are_given_in()
			throws Exception {
		final byte[] a = createClass("a");
		final byte[] b = createClass("b");
		final File dir1 = createClassDir("dir1", a);
		final File dir2 = createClassDir("dir2", b);
		final File exec = createExecFile(b);
		final File xml = new File(tmp.getRoot(), "coverage.xml");

		execute("report", exec.getAbsolutePath(), "--classfiles",
				dir2.getAbsolutePath(), "--classfiles", dir1.getAbsolutePath(),
				"--resolveduplicates", "--xml", xml.getAbsolutePath());

		assertOk();
		assertContains("[WARN] Class Foo: reporting " + classFile(dir2)
				+ ", ignoring " + classFile(dir1) + ".", out);
		assertReported(xml, "b", "a");
	}

	@Test
	public void should_report_copy_with_lowest_class_id_when_no_copy_has_execution_data()
			throws Exception {
		final byte[] a = createClass("a");
		final byte[] b = createClass("b");
		final boolean aIsLower = CRC64.classId(a) < CRC64.classId(b);
		final File dirA = createClassDir("dirA", a);
		final File dirB = createClassDir("dirB", b);
		final File xml = new File(tmp.getRoot(), "coverage.xml");

		execute("report", "--classfiles", dirA.getAbsolutePath(),
				"--classfiles", dirB.getAbsolutePath(), "--resolveduplicates",
				"--xml", xml.getAbsolutePath());

		assertOk();
		final File kept = aIsLower ? dirA : dirB;
		final File ignored = aIsLower ? dirB : dirA;
		assertContains("[WARN] Class Foo: reporting " + classFile(kept)
				+ ", ignoring " + classFile(ignored) + ".", out);
		assertReported(xml, aIsLower ? "a" : "b", aIsLower ? "b" : "a");
	}

	@Test
	public void should_print_warning_when_execution_data_was_recorded_for_several_copies()
			throws Exception {
		final byte[] a = createClass("a");
		final byte[] b = createClass("b");
		final boolean aIsLower = CRC64.classId(a) < CRC64.classId(b);
		final File dirA = createClassDir("dirA", a);
		final File dirB = createClassDir("dirB", b);
		final File exec = createExecFile(a, b);
		final File xml = new File(tmp.getRoot(), "coverage.xml");

		execute("report", exec.getAbsolutePath(), "--classfiles",
				dirA.getAbsolutePath(), "--classfiles", dirB.getAbsolutePath(),
				"--resolveduplicates", "--xml", xml.getAbsolutePath());

		assertOk();
		assertContains(
				"[WARN] Class Foo: execution data was recorded for 2 copies, the coverage of the ignored ones is not reported.",
				out);
		assertReported(xml, aIsLower ? "a" : "b", aIsLower ? "b" : "a");
	}

	@Test
	public void should_name_archive_entries_of_duplicate_classes()
			throws Exception {
		final byte[] a = createClass("a");
		final byte[] b = createClass("b");
		final File jar = new File(tmp.getRoot(), "app.jar");
		final ZipOutputStream zip = new ZipOutputStream(
				new FileOutputStream(jar));
		zip.putNextEntry(new ZipEntry("Foo.class"));
		zip.write(a);
		zip.putNextEntry(new ZipEntry("hidden/Foo.class_hidden"));
		zip.write(b);
		zip.close();
		final File exec = createExecFile(b);

		execute("report", exec.getAbsolutePath(), "--classfiles",
				jar.getAbsolutePath(), "--resolveduplicates");

		assertOk();
		assertContains("[WARN] Class Foo: reporting " + jar.getAbsolutePath()
				+ "@hidden/Foo.class_hidden, ignoring " + jar.getAbsolutePath()
				+ "@Foo.class.", out);
	}

	private static byte[] createClass(final String method) {
		final ClassWriter writer = new ClassWriter(0);
		writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Foo", null,
				"java/lang/Object", null);
		final MethodVisitor mv = writer.visitMethod(Opcodes.ACC_PUBLIC, method,
				"()V", null, null);
		mv.visitCode();
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 1);
		mv.visitEnd();
		writer.visitEnd();
		return writer.toByteArray();
	}

	private File createClassDir(final String name, final byte[] bytes)
			throws IOException {
		final File dir = tmp.newFolder(name);
		final FileOutputStream file = new FileOutputStream(classFile(dir));
		file.write(bytes);
		file.close();
		return dir;
	}

	private static File classFile(final File dir) {
		return new File(dir, "Foo.class");
	}

	private File createExecFile(final byte[]... classes) throws IOException {
		final File exec = new File(tmp.getRoot(), "jacoco.exec");
		final FileOutputStream execout = new FileOutputStream(exec);
		final ExecutionDataWriter writer = new ExecutionDataWriter(execout);
		for (final byte[] c : classes) {
			writer.visitClassExecution(new ExecutionData(CRC64.classId(c),
					"Foo", new boolean[] { true }));
		}
		execout.close();
		return exec;
	}

	private static void assertReported(final File xml, final String reported,
			final String ignored) throws IOException {
		final FileInputStream in = new FileInputStream(xml);
		final String content;
		try {
			content = new String(InputStreams.readFully(in), "UTF-8");
		} finally {
			in.close();
		}
		final String clazz = "<class name=\"Foo\"";
		assertEquals(content, content.indexOf(clazz),
				content.lastIndexOf(clazz));
		assertTrue(content,
				content.contains("<method name=\"" + reported + "\""));
		assertFalse(content,
				content.contains("<method name=\"" + ignored + "\""));
	}

}
