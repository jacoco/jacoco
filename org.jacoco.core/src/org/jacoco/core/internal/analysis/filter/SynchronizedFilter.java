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
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * Filters code that is generated for synchronized statement.
 */
public final class SynchronizedFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		for (final TryCatchBlockNode tryCatch : methodNode.tryCatchBlocks) {
			if (tryCatch.type != null) {
				continue;
			}
			if (tryCatch.start == tryCatch.handler) {
				continue;
			}
			final AbstractInsnNode to = new Matcher(tryCatch.handler).match();
			if (to == null) {
				continue;
			}
			output.ignore(tryCatch.handler, to);
		}
	}

	private static class Matcher extends AbstractMatcher {
		private final AbstractInsnNode start;

		private Matcher(final AbstractInsnNode start) {
			this.start = start;
		}

		private AbstractInsnNode match() {
			if (nextIsEcj() || nextIsJavac()) {
				return cursor;
			}
			return null;
		}

		private boolean nextIsJavac() {
			cursor = start;
			nextIsVar(Opcodes.ASTORE, "t");
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.MONITOREXIT);
			nextIsVar(Opcodes.ALOAD, "t");
			nextIs(Opcodes.ATHROW);
			return cursor != null;
		}

		private boolean nextIsEcj() {
			cursor = start;
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.MONITOREXIT);
			nextIs(Opcodes.ATHROW);
			return cursor != null;
		}
	}

}
