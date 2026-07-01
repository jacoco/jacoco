/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.snapshot;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;

import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.test.TargetLoader;
import org.junit.ComparisonFailure;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * Utilities for working with snapshots (textual representations) of methods
 * bytecode.
 */
public final class MethodSnapshot {

	/**
	 * Returns snapshot (textual representation) of given {@code methodNode}
	 * with line numbers normalized to start from 5 as if method is the first in
	 * Java source file
	 *
	 * <pre>
	 * package org.example;
	 *
	 * class Example {
	 * 	void method() {
	 * 		System.out.println(); // line number 5
	 * 	}
	 * }
	 * </pre>
	 */
	public static String snapshot(final MethodNode methodNode) {
		final Textifier textifier = new Textifier();
		methodNode.accept(
				new MethodSnapshotVisitor(new TraceMethodVisitor(textifier)));
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);
		textifier.print(printWriter);
		printWriter.flush();
		return stringWriter.toString();
	}

	/**
	 * @return {@link MethodNode} for {@link #snapshot(MethodNode)} from given
	 *         {@code reader}
	 */
	public static MethodNode parse(final Reader reader,
			final MethodSnapshotCommentsHandler commentsHandler)
			throws IOException {
		final MethodNode methodNode = new MethodNode();
		new MethodSnapshotParser(reader, methodNode, commentsHandler).parse();
		return methodNode;
	}

	/**
	 * Compares {@link #snapshot(MethodNode)} of {@code targetMethod} in
	 * {@code targetClass} with snapshot of
	 * {@link #parse(Reader, MethodSnapshotCommentsHandler) parsed}
	 * {@code snapshotFile}, in case of differences creates
	 * {@code newSnapshotFile} and throws {@link ComparisonFailure}.
	 *
	 * {@code snapshotFile} can be modified by
	 * <ul>
	 * <li>renaming labels</li>
	 * <li>adding comments between instructions</li>
	 * </ul>
	 * without impact on this comparison.
	 */
	public static void compare(final Class<?> targetClass,
			final String targetMethod, final File snapshotFile,
			final File newSnapshotFile) throws IOException {
		final String expected;
		if (snapshotFile.exists()) {
			final FileReader reader = new FileReader(snapshotFile);
			final MethodNode parsed = parse(reader, null);
			reader.close();
			expected = snapshot(parsed);
		} else {
			expected = "// NOT FOUND";
		}
		final byte[] classBytes = TargetLoader.getClassDataAsBytes(targetClass);
		final ClassNode classNode = new ClassNode();
		InstrSupport.classReaderFor(classBytes).accept(classNode,
				ClassReader.SKIP_FRAMES);
		final MethodNode methodNode = find(classNode, targetMethod);
		final String actual = snapshot(methodNode);
		if (actual.equals(expected)) {
			return;
		}
		newSnapshotFile.getParentFile().mkdirs();
		final FileWriter fileWriter = new FileWriter(newSnapshotFile);
		fileWriter.write(actual);
		fileWriter.close();
		final String message = String.format("%n" + //
				"actual %s%n" + //
				"file://%s%n" + //
				"should match expected file://%s%n", //
				location(classNode, methodNode),
				newSnapshotFile.getAbsolutePath(),
				snapshotFile.getAbsolutePath());
		throw new ComparisonFailure(message, expected, actual);
	}

	private static String location(final ClassNode classNode,
			final MethodNode methodNode) {
		return String.format("%s.%s(%s:%d)", classNode.name, methodNode.name,
				classNode.sourceFile, firstLineNumber(methodNode));
	}

	/**
	 * @return first line number in given {@code methodNode}, or {@code -1}
	 */
	private static int firstLineNumber(final MethodNode methodNode) {
		for (AbstractInsnNode i : methodNode.instructions) {
			if (i.getType() == AbstractInsnNode.LINE) {
				return ((LineNumberNode) i).line;
			}
		}
		return -1;
	}

	/**
	 * @return method with given {@code name} from given {@code classNode}
	 */
	private static MethodNode find(final ClassNode classNode,
			final String name) {
		MethodNode result = null;
		for (MethodNode methodNode : classNode.methods) {
			if (methodNode.name.equals(name)) {
				assertNull(result);
				result = methodNode;
			}
		}
		assertNotNull(result);
		return result;
	}

	private MethodSnapshot() {
	}
}
