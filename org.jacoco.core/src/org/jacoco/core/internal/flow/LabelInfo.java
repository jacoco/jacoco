/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.flow;

import org.objectweb.asm.Label;

/**
 * Data container that is attached to {@link Label#info} objects to store flow
 * and instrumentation specific information.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public final class LabelInfo {

	private boolean target = false;

	private boolean multiTarget = false;

	private boolean successor = false;

	// instances are only created within this class
	private LabelInfo() {
	}

	/**
	 * Defines that the given label is a jump target.
	 * 
	 * @param label
	 *            label to define
	 */
	public static void setTarget(final Label label) {
		final LabelInfo info = create(label);
		if (info.target || info.successor) {
			info.multiTarget = true;
		} else {
			info.target = true;
		}
	}

	/**
	 * Defines that the given label is the possible successor of the previous
	 * instruction in the method.
	 * 
	 * @param label
	 *            label to define
	 */
	public static void setSuccessor(final Label label) {
		final LabelInfo info = create(label);
		info.successor = true;
		if (info.target) {
			info.multiTarget = true;
		}
	}

	/**
	 * Checks whether multiple control paths lead to a label. Control flow path
	 * to a certain label are: jump targets, exception handlers and normal
	 * control flow from its predecessor instruction (unless this a
	 * unconditional jump or method exit).
	 * 
	 * @param label
	 *            label to check
	 * @return <code>true</code> if the given multiple control paths lead to the
	 *         given label
	 */
	public static boolean isMultiTarget(final Label label) {
		final LabelInfo info = get(label);
		return info == null ? false : info.multiTarget;
	}

	/**
	 * Checks whether this label is the possible successor of the previous
	 * instruction in the method. This is the case if the predecessor isn't a
	 * unconditional jump or method exit instruction.
	 * 
	 * @param label
	 *            label to check
	 * @return <code>true</code> if the label is a possible instruction
	 *         successor
	 */
	public static boolean isSuccessor(final Label label) {
		final LabelInfo info = get(label);
		return info == null ? false : info.successor;
	}

	private static LabelInfo get(final Label label) {
		return (LabelInfo) label.info;
	}

	private static LabelInfo create(final Label label) {
		LabelInfo info = get(label);
		if (info == null) {
			info = new LabelInfo();
			label.info = info;
		}
		return info;
	}

}
