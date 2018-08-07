/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters bytecode that Kotlin compiler generates for <code>when</code>
 * expressions which list all cases of <code>sealed class</code>, i.e. which
 * don't require explicit <code>else</code>.
 */
public final class KotlinWhenSealedFilter implements IFilter {

	private static final String EXCEPTION = "kotlin/NoWhenBranchMatchedException";

	private final Matcher matcher = new Matcher();

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		for (AbstractInsnNode i = methodNode.instructions
				.getFirst(); i != null; i = i.getNext()) {
			matcher.match(i, output);
		}
	}

	private static class Matcher extends AbstractMatcher {
		void match(final AbstractInsnNode start, final IFilterOutput output) {
			if (start.getType() != InsnNode.LABEL) {
				return;
			}
			cursor = start;

			nextIsNew(EXCEPTION);
			nextIs(Opcodes.DUP);
			nextIsInvokeSuper(EXCEPTION, "()V");
			nextIs(Opcodes.ATHROW);

			for (AbstractInsnNode i = cursor; i != null; i = i.getPrevious()) {
				if (i.getOpcode() == Opcodes.IFEQ
						&& ((JumpInsnNode) i).label == start) {
					output.ignore(i, i);
					output.ignore(start, cursor);
					return;
				}
			}
		}
	}

}
