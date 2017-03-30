/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.pattern;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * A collection of static factory methods to create {@link IPattern} instances.
 */
public final class Patterns {

	private Patterns() {
	}

	/**
	 * Combines a sequence of patterns to a new pattern which only matches if
	 * the patterns match in the given sequence.
	 * 
	 * @param sequence
	 *            sequence of patterns
	 * @return pattern for the sequence
	 */
	public static IPattern sequence(final IPattern... sequence) {
		return new IPattern() {
			public AbstractInsnNode matchForward(AbstractInsnNode node) {
				boolean first = true;
				for (final IPattern p : sequence) {
					if (!first) {
						node = node.getNext();
					}
					first = false;
					if (node == null) {
						return null;
					}
					node = p.matchForward(node);
					if (node == null) {
						return null;
					}
				}
				return node;
			}
		};
	}

	/**
	 * Creates a new matcher that matches if any of the given patterns matches.
	 * 
	 * @param choice
	 *            list of patterns to check one after the other
	 * @return pattern for the choice
	 */
	public static IPattern choice(final IPattern... choice) {
		return new IPattern() {
			public AbstractInsnNode matchForward(final AbstractInsnNode node) {
				for (final IPattern p : choice) {
					final AbstractInsnNode result = p.matchForward(node);
					if (result != null) {
						return result;
					}
				}
				return null;
			}
		};
	}

	/**
	 * Pattern for a single ALOAD instruction
	 */
	public static IPattern ALOAD = new SingleInstructionPattern(Opcodes.ALOAD);

	/**
	 * Pattern for a single ASTORE instruction
	 */
	public static IPattern ASTORE = new SingleInstructionPattern(Opcodes.ASTORE);

	/**
	 * Pattern for a single ATHROW instruction
	 */
	public static IPattern ATHROW = new SingleInstructionPattern(Opcodes.ATHROW);

	/**
	 * Pattern for a single MONITOREXIT instruction
	 */
	public static IPattern MONITOREXIT = new SingleInstructionPattern(
			Opcodes.MONITOREXIT);

	private static class SingleInstructionPattern implements IPattern {
		private final int opcode;

		SingleInstructionPattern(final int opcode) {
			this.opcode = opcode;

		}

		public AbstractInsnNode matchForward(AbstractInsnNode node) {
			node = skipNonInstructions(node);
			if (node != null && node.getOpcode() == opcode) {
				return node;
			}
			return null;
		}

		private AbstractInsnNode skipNonInstructions(AbstractInsnNode node) {
			while (node != null) {
				final int type = node.getType();
				if ((type != AbstractInsnNode.FRAME)
						&& (type != AbstractInsnNode.LABEL)
						&& (type != AbstractInsnNode.LINE)) {
					return node;
				}
				node = node.getNext();
			}
			return null;
		}
	}

}
