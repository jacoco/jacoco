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

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
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
				|| matcher.isToString(methodNode)
				|| matcher.isFieldAccessor(methodNode)) {
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

		/**
		 * Criteria: method name == field name, only three instructions (aload0,
		 * getField, return), and note that this class only happens in a record,
		 * so it's safe to assume that this is the record field accessor
		 * generated. It may happen that the code is explicitly written by the
		 * developer and is intentionally kept the same as the default generated
		 * format, but that's just trivial code, and it still makes sense to
		 * filter them out anyway.
		 * <p>
		 * Exception: if the code is compiled within IntelliJ IDEA's Java
		 * instrumentation, there will be extra null-assertion instructions
		 * after the getField instruction. This case is <emph>ignored</emph>.
		 */
		boolean isFieldAccessor(final MethodNode m) {
			if (!m.desc.startsWith("()")) {
				// Method with parameter(s)
				return false;
			}
			firstIsALoad0(m);
			nextIs(Opcodes.GETFIELD);
			if (!(cursor instanceof FieldInsnNode)) {
				return false;
			}
			if (!((FieldInsnNode) cursor).name.equals(m.name)) {
				return false;
			}
			next();
			if (cursor == null) {
				return false;
			}
			switch (cursor.getOpcode()) {
			case Opcodes.IRETURN:
			case Opcodes.LRETURN:
			case Opcodes.FRETURN:
			case Opcodes.DRETURN:
			case Opcodes.ARETURN:
				return true;
			default:
				return false;
			}
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
