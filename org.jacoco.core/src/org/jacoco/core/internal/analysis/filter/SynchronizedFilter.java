/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
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

import static org.jacoco.core.internal.analysis.pattern.Patterns.ALOAD;
import static org.jacoco.core.internal.analysis.pattern.Patterns.ASTORE;
import static org.jacoco.core.internal.analysis.pattern.Patterns.ATHROW;
import static org.jacoco.core.internal.analysis.pattern.Patterns.MONITOREXIT;
import static org.jacoco.core.internal.analysis.pattern.Patterns.choice;
import static org.jacoco.core.internal.analysis.pattern.Patterns.sequence;

import org.jacoco.core.internal.analysis.pattern.IPattern;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * Filters code that is generated for synchronized statement.
 */
public final class SynchronizedFilter implements IFilter {

	private static final IPattern EXCEPTIONAL_EXIT = choice( //

			// javac
			sequence(ASTORE, ALOAD, MONITOREXIT, ALOAD, ATHROW),

			// ecj
			sequence(ALOAD, MONITOREXIT, ATHROW)

	);

	public void filter(final String className, final String superClassName,
			final MethodNode methodNode, final IFilterOutput output) {
		for (final TryCatchBlockNode tryCatch : methodNode.tryCatchBlocks) {
			if (tryCatch.type != null) {
				continue;
			}
			if (tryCatch.start == tryCatch.handler) {
				continue;
			}
			final AbstractInsnNode toNode = EXCEPTIONAL_EXIT
					.matchForward(tryCatch.handler);
			if (toNode != null) {
				output.ignore(tryCatch.handler, toNode);
			}
		}
	}

}
