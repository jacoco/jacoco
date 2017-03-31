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

/**
 * Internally used context passed through the {@link IPattern}s while matching.
 * A context can only be used once for matching.
 */
public class MatchContext {

	private String[] locals = null;

	boolean isLocal(final int var, final String name) {
		if (locals == null) {
			return false;
		}
		if (locals.length <= var) {
			return false;
		}
		return name.equals(locals[var]);
	}

	void setLocal(final int var, final String name) {
		if (locals == null) {
			locals = new String[(var + 1) * 2];
		}
		if (locals.length <= var) {
			final String[] newLocals = new String[(var + 1) * 2];
			System.arraycopy(locals, 0, newLocals, 0, locals.length);
			locals = newLocals;
		}
		locals[var] = name;
	}

}
