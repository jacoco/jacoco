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
import org.objectweb.asm.tree.MethodNode;

/**
 * Filters empty enum constructors.
 *
 * Constructor of enum is invoked from static initialization block to create
 * instance of each enum constant. So it won't be executed if number of enum
 * constants is zero. Such enums are sometimes used as alternative to classes
 * with static utilities and private empty constructor. Implicit constructor of
 * enum created by compiler doesn't have a synthetic flag and refers to a line
 * of enum definition. Therefore in order to not have partial coverage of enum
 * definition line in enums without enum constants and similarly to
 * {@link PrivateEmptyNoArgConstructorFilter filter of private empty
 * constructors} - empty constructor in enums without additional parameters
 * should be filtered out even if it is not implicit.
 */
public final class EnumEmptyConstructorFilter implements IFilter {

	private static final String CONSTRUCTOR_NAME = "<init>";
	private static final String CONSTRUCTOR_DESC = "(Ljava/lang/String;I)V";

	private static final String ENUM_TYPE = "java/lang/Enum";

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		if (ENUM_TYPE.equals(context.getSuperClassName())
				&& CONSTRUCTOR_NAME.equals(methodNode.name)
				&& CONSTRUCTOR_DESC.equals(methodNode.desc)
				&& new Matcher().match(methodNode)) {
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
		}
	}

	private static class Matcher extends AbstractMatcher {
		private boolean match(final MethodNode methodNode) {
			firstIsALoad0(methodNode);
			nextIs(Opcodes.ALOAD);
			nextIs(Opcodes.ILOAD);
			nextIsInvoke(Opcodes.INVOKESPECIAL, ENUM_TYPE, CONSTRUCTOR_NAME,
					CONSTRUCTOR_DESC);
			nextIs(Opcodes.RETURN);
			return cursor != null;
		}
	}

}
