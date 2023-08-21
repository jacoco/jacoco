/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lars Grefer - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.tree.MethodNode;

public class AspectJSyntheticFilter implements IFilter {
	@Override
	public void filter(MethodNode methodNode, IFilterContext context,
			IFilterOutput output) {

		if (isAjSynthetic(methodNode)) {
			output.ignore(methodNode.instructions.getFirst(),
					methodNode.instructions.getLast());
		}
	}

	public static boolean isAjSynthetic(MethodNode methodNode) {
		if (methodNode.attrs == null) {
			return false;
		}

		for (Attribute attr : methodNode.attrs) {
			if (attr.type.equals("org.aspectj.weaver.AjSynthetic")) {
				return true;
			}
		}

		return false;
	}
}
