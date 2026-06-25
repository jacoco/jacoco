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
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jacoco.core.analysis.ISourceFileProvider;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link SourceFilter}.
 */
public class SourceFilterTest {

	private SourceFilter filter;
	private ISourceFileProvider provider;
	private String sourceContent;

	private MethodNode methodNode;
	private IFilterContext context;
	private Set<AbstractInsnNode> ignored;

	@Before
	public void setup() {
		provider = new ISourceFileProvider() {
			public java.io.Reader getSourceFile(String packageName,
					String fileName) throws IOException {
				if (sourceContent == null) {
					return null;
				}
				return new StringReader(sourceContent);
			}
		};
		filter = new SourceFilter(provider);
		methodNode = new MethodNode();
		context = new IFilterContext() {

			public String getSuperClassName() {
				return "java/lang/Object";
			}

			public String getSourceFileName() {
				return "Source.java";
			}

			public String getSourceDebugExtension() {
				return null;
			}

			public String getClassName() {
				return "org/example/Source";
			}

			public Set<String> getClassAttributes() {
				return Collections.emptySet();
			}

			public Set<String> getClassAnnotations() {
				return Collections.emptySet();
			}
		};
		ignored = new HashSet<AbstractInsnNode>();
	}

	@Test
	public void should_filter_lines_between_off_and_on_tags() {
		sourceContent = "line 1\n" + "//jacoco:off\n" + "line 3\n" + "line 4\n"
				+ "//jacoco:on\n" + "line 6";

		addInstruction(1);
		addInstruction(2);
		AbstractInsnNode i3 = addInstruction(3);
		AbstractInsnNode i4 = addInstruction(4);
		addInstruction(5);
		addInstruction(6);

		filter.filter(methodNode, context, new IFilterOutput() {
			public void replaceBranches(AbstractInsnNode source,
					Replacements replacements) {
			}

			public void merge(AbstractInsnNode i1, AbstractInsnNode i2) {
			}

			public void ignore(AbstractInsnNode fromInclusive,
					AbstractInsnNode toInclusive) {
				ignored.add(fromInclusive);
			}
		});

		assertEquals(2, ignored.size());
		assertEquals(true, ignored.contains(i3));
		assertEquals(true, ignored.contains(i4));
	}

	@Test
	public void should_not_filter_if_source_not_found() {
		sourceContent = null;

		AbstractInsnNode i3 = addInstruction(3);

		filter.filter(methodNode, context, new IFilterOutput() {
			public void replaceBranches(AbstractInsnNode source,
					Replacements replacements) {
			}

			public void merge(AbstractInsnNode i1, AbstractInsnNode i2) {
			}

			public void ignore(AbstractInsnNode fromInclusive,
					AbstractInsnNode toInclusive) {
				ignored.add(fromInclusive);
			}
		});

		assertEquals(0, ignored.size());
	}

	@Test
	public void should_ignore_till_end_if_on_tag_missing() {
		sourceContent = "line 1\n" + "//jacoco:off\n" + "line 3";

		addInstruction(1);
		AbstractInsnNode i3 = addInstruction(3);

		filter.filter(methodNode, context, new IFilterOutput() {
			public void replaceBranches(AbstractInsnNode source,
					Replacements replacements) {
			}

			public void merge(AbstractInsnNode i1, AbstractInsnNode i2) {
			}

			public void ignore(AbstractInsnNode fromInclusive,
					AbstractInsnNode toInclusive) {
				ignored.add(fromInclusive);
			}
		});

		assertEquals(1, ignored.size());
		assertEquals(true, ignored.contains(i3));
	}

	private AbstractInsnNode addInstruction(int line) {
		MethodNode m = methodNode;
		m.visitLineNumber(line, new org.objectweb.asm.Label());
		AbstractInsnNode insn = new InsnNode(Opcodes.NOP);
		m.instructions.add(insn);
		return insn;
	}
}
