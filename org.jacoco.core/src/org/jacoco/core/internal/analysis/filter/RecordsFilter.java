/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
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

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters methods <code>toString</code>, <code>hashCode</code> and
 * <code>equals</code> that compiler generates for records.
 */
public final class RecordsFilter implements IFilter {

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (!"java/lang/Record".equals(context.getSuperClassName())) {
			return;
		}
		final Matcher matcher = new Matcher();
		if (matcher.isEquals(methodNode) || matcher.isHashCode(methodNode)
				|| matcher.isToString(methodNode)) {
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
		}
	}

	private static class Matcher extends AbstractMatcher {
		boolean isToString(final MethodNode m) {
			if (!"toString".equals(m.name)
					|| !"()Ljava/lang/String;".equals(m.desc)) {
				return false;
			}
			firstIsALoad0(m);
			nextIsInvokeDynamic("toString");
			nextIs(Opcodes.ARETURN);
			return cursor != null;
		}

		boolean isHashCode(final MethodNode m) {
			if (!"hashCode".equals(m.name) || !"()I".equals(m.desc)) {
				return false;
			}
			firstIsALoad0(m);
			nextIsInvokeDynamic("hashCode");
			nextIs(Opcodes.IRETURN);
			return cursor != null;
		}

		boolean isEquals(final MethodNode m) {
			if (!"equals".equals(m.name)
					|| !"(Ljava/lang/Object;)Z".equals(m.desc)) {
				return false;
			}
			firstIsALoad0(m);
			nextIs(Opcodes.ALOAD);
			nextIsInvokeDynamic("equals");
			nextIs(Opcodes.IRETURN);
			return cursor != null;
		}

		private void nextIsInvokeDynamic(final String name) {
			nextIs(Opcodes.INVOKEDYNAMIC);
			if (cursor == null) {
				return;
			}
			final InvokeDynamicInsnNode i = (InvokeDynamicInsnNode) cursor;
			final Handle bsm = i.bsm;
			if (name.equals(i.name)
					&& "java/lang/runtime/ObjectMethods".equals(bsm.getOwner())
					&& "bootstrap".equals(bsm.getName())) {
				return;
			}
			cursor = null;
		}
	}

}
