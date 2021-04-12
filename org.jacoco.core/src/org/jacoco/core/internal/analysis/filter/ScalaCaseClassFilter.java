/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Arvind Waiker - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.analysis.filter;

import org.objectweb.asm.tree.MethodNode;

import java.util.Iterator;

/**
 * Filter to ignore methods for Case Classes in Scala.
 * Warning: This will ignore equals and hashcode and other methods written for all the scala classes
 */
public final class ScalaCaseClassFilter implements IFilter {

	private static boolean isScalaClass(final IFilterContext context) {
		return context.getClassAttributes().contains("ScalaSig")
				|| context.getClassAttributes().contains("Scala");
	}

	public void filter(final MethodNode methodNode,
			final IFilterContext context, final IFilterOutput output) {
		Iterator<String> it = context.getClassAnnotations().iterator();
		while(it.hasNext()) {
			System.out.println(it.next());
		}
		if (isScalaClass(context)) {
			if ("canEqual".equals(methodNode.name)
					|| "copy".equals(methodNode.name)
					|| "equals".equals(methodNode.name)
					|| "hashCode".equals(methodNode.name)
					|| "productPrefix".equals(methodNode.name)
					|| "productArity".equals(methodNode.name)
					|| "productElement".equals(methodNode.name)
					|| "productElementNames".equals(methodNode.name)
					|| "productElementName".equals(methodNode.name)
					|| "productIterator".equals(methodNode.name)
					|| "toString".equals(methodNode.name)
					|| methodNode.name.startsWith("copy$default")) {
				output.ignore(methodNode.instructions.getFirst(),
						methodNode.instructions.getLast());
			}
		}
	}

}
