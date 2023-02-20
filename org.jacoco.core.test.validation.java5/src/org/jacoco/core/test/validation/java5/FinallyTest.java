/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.java5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.test.TargetLoader;
import org.jacoco.core.test.validation.JavaVersion;
import org.jacoco.core.test.validation.Source.Line;
import org.jacoco.core.test.validation.ValidationTestBase;
import org.jacoco.core.test.validation.java5.targets.FinallyTarget;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Test of filtering of duplicated bytecode that is generated for finally block.
 */
public class FinallyTest extends ValidationTestBase {

	private Map<Integer, String> tags;

	public FinallyTest() {
		super(FinallyTarget.class);
	}

	@Before
	@Override
	public void setup() throws Exception {
		super.setup();
		tags = new HashMap<Integer, String>();
	}

	public void assertFinally(final Line line) {
		if (isJDKCompiler) {
			assertEmpty(line);
		} else {
			assertFullyCovered(line);
		}
	}

	public void assertTwoRegions1(final Line line) {
		if (isJDKCompiler && JavaVersion.current().isBefore("1.8")) {
			// https://bugs.openjdk.java.net/browse/JDK-7008643
			assertPartlyCovered(line);
		} else {
			assertFullyCovered(line);
		}
	}

	public void assertTwoRegionsReturn1(final Line line) {
		if (isJDKCompiler && JavaVersion.current().isBefore("1.8")) {
			// https://bugs.openjdk.java.net/browse/JDK-7008643
			assertEmpty(line);
		} else {
			assertFullyCovered(line);
		}
	}

	public void assertTwoRegionsReturn2(final Line line) {
		if (isJDKCompiler && JavaVersion.current().isBefore("1.8")) {
			// https://bugs.openjdk.java.net/browse/JDK-7008643
			assertEmpty(line);
		} else {
			assertNotCovered(line);
		}
	}

	public void assertEmptyTry1(final Line line) {
		if (isJDKCompiler && JavaVersion.current().isBefore("1.8")) {
			// compiler bug fixed in javac >= 1.8:
			assertPartlyCovered(line);
		} else {
			assertFullyCovered(line);
		}
	}

	public void assertEmptyTry2(final Line line) {
		if (isJDKCompiler && JavaVersion.current().isBefore("1.8")) {
			// compiler bug fixed in javac >= 1.8:
			assertFullyCovered(line);
		} else {
			assertEmpty(line);
		}
	}

	public void assertAlwaysCompletesAbruptly0(final Line line) {
		if (isJDKCompiler) {
			// uncovered case:
			assertEmpty(line);
		} else {
			assertPartlyCovered(line);
		}
	}

	public void assertAlwaysCompletesAbruptly1(final Line line) {
		if (isJDKCompiler) {
			// uncovered case:
			assertPartlyCovered(line);
		} else {
			assertFullyCovered(line);
		}
	}

	@Test
	@Override
	public void execute_assertions_in_comments() throws IOException {
		super.execute_assertions_in_comments();
		gotos();
	}

	/**
	 * This test studies placement of GOTO instructions.
	 */
	private void gotos() throws IOException {

		final Set<String> expected = new HashSet<String>();

		if (isJDKCompiler) {
			expected.add("example.2");
		} else {
			expected.add("example.0");
		}

		expected.add("breakStatement.for");
		if (isJDKCompiler) {
			if (JavaVersion.current().isBefore("10")) {
				// https://bugs.openjdk.java.net/browse/JDK-8180141
				expected.add("breakStatement.1");
			} else {
				expected.add("breakStatement");
			}
			expected.add("breakStatement.2");
		} else {
			expected.add("breakStatement");
		}

		if (isJDKCompiler) {
			expected.add("emptyCatch.2");
		} else {
			expected.add("emptyCatch");
			expected.add("emptyCatch.1");
		}

		if (isJDKCompiler) {
			expected.add("catchNotExecuted.2");
		} else {
			expected.add("catchNotExecuted");
			expected.add("catchNotExecuted.1");
		}

		if (isJDKCompiler) {
			expected.add("nested.5");
			expected.add("nested.6");
		} else {
			expected.add("nested.0");
			expected.add("nested.3");
		}

		if (isJDKCompiler && JavaVersion.current().isBefore("1.8")) {
			expected.add("emptyTry.2");
		}

		if (!isJDKCompiler) {
			expected.add("alwaysCompletesAbruptly.0");
		}

		assertEquals(expected, getTagsWithGotos());
	}

	private Set<String> getTagsWithGotos() throws IOException {
		final Set<String> gotoTags = new HashSet<String>();

		byte[] b = TargetLoader.getClassDataAsBytes(FinallyTarget.class);

		final ClassNode classNode = new ClassNode();
		InstrSupport.classReaderFor(b).accept(classNode, 0);
		for (final MethodNode m : classNode.methods) {
			if ("main".equals(m.name)) {
				// skip it
				continue;
			}
			int lineNumber = -1;
			for (AbstractInsnNode i : m.instructions) {
				if (AbstractInsnNode.LINE == i.getType()) {
					lineNumber = ((LineNumberNode) i).line;
				}
				if (Opcodes.GOTO == i.getOpcode()) {
					String tag = tags.get(Integer.valueOf(lineNumber));
					if (tag == null) {
						throw new AssertionError(
								"No tag at line " + lineNumber);
					}
					gotoTags.add(tag);
				}
			}
		}

		return gotoTags;
	}

	public void tag(final Line line, String tag) {
		assertFalse("duplicate tag " + tag, tags.containsValue(tag));
		assertNull("duplicate tag in " + line,
				tags.put(Integer.valueOf(line.getNr()), tag));
	}

}
