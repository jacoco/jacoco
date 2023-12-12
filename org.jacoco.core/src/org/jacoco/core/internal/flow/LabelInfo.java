/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.flow;

import org.jacoco.core.internal.analysis.Instruction;
import org.objectweb.asm.Label;

/**
 * Data container that is attached to {@link Label#info} objects to store flow
 * and instrumentation specific information. The information is only valid
 * locally in specific contexts.
 */
public final class LabelInfo {

	/**
	 * Reserved ID for "no probe".
	 */
	public static final int NO_PROBE = -1;

	private boolean target = false;

	private boolean multiTarget = false;

	private boolean successor = false;

	private boolean methodInvocationLine = false;

	private boolean done = false;

	private int probeid = NO_PROBE;

	private Label intermediate = null;

	private Instruction instruction = null;

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
	 * control flow from its predecessor instruction (unless this is an
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

	/**
	 * Mark a given label as the beginning of a line with method invocations.
	 *
	 * @param label
	 *            label to mark
	 */
	public static void setMethodInvocationLine(final Label label) {
		create(label).methodInvocationLine = true;
	}

	/**
	 * Checks whether the a given label has been marked as a line with method
	 * invocations.
	 *
	 * @param label
	 *            label to check
	 * @return <code>true</code> if the label represents a line with method
	 *         invocations
	 */
	public static boolean isMethodInvocationLine(final Label label) {
		final LabelInfo info = get(label);
		return info == null ? false : info.methodInvocationLine;
	}

	/**
	 * Determines whether the given label needs a probe to be inserted before.
	 *
	 * @param label
	 *            label to test
	 * @return <code>true</code> if a probe should be inserted before
	 */
	public static boolean needsProbe(final Label label) {
		final LabelInfo info = get(label);
		return info != null && info.successor
				&& (info.multiTarget || info.methodInvocationLine);
	}

	/**
	 * Mark a given label as done.
	 *
	 * @param label
	 *            label to mark
	 */
	public static void setDone(final Label label) {
		create(label).done = true;
	}

	/**
	 * Resets the "done" status of a given label.
	 *
	 * @param label
	 *            label to reset
	 */
	public static void resetDone(final Label label) {
		final LabelInfo info = get(label);
		if (info != null) {
			info.done = false;
		}
	}

	/**
	 * Resets the "done" status of all given labels.
	 *
	 * @param labels
	 *            labels to reset
	 */
	public static void resetDone(final Label[] labels) {
		for (final Label label : labels) {
			resetDone(label);
		}
	}

	/**
	 * Checks whether this label is marked as done.
	 *
	 * @param label
	 *            label to check
	 * @return <code>true</code> if this label is marked as done
	 */
	public static boolean isDone(final Label label) {
		final LabelInfo info = get(label);
		return info == null ? false : info.done;
	}

	/**
	 * Sets the given probe id to the given label.
	 *
	 * @param label
	 *            label to assign a probe to
	 * @param id
	 *            id of the probe
	 */
	public static void setProbeId(final Label label, final int id) {
		create(label).probeid = id;
	}

	/**
	 * Returns the assigned probe id.
	 *
	 * @param label
	 *            label to check
	 * @return probe id or {@link #NO_PROBE} if no probe is assigned to the
	 *         label
	 */
	public static int getProbeId(final Label label) {
		final LabelInfo info = get(label);
		return info == null ? NO_PROBE : info.probeid;
	}

	/**
	 * Defines an intermediate label for the given label. Such intermediate
	 * labels are required during instrumentation to add probes to jump targets.
	 *
	 * @param label
	 *            label to define for
	 * @param intermediate
	 *            intermediate label
	 */
	public static void setIntermediateLabel(final Label label,
			final Label intermediate) {
		create(label).intermediate = intermediate;
	}

	/**
	 * Returns the intermediate label for the given label if one has been
	 * defined.
	 *
	 * @param label
	 *            label to look for
	 * @return intermediate label or <code>null</code>
	 */
	public static Label getIntermediateLabel(final Label label) {
		final LabelInfo info = get(label);
		return info == null ? null : info.intermediate;
	}

	/**
	 * Sets the instruction corresponding to this label.
	 *
	 * @param label
	 *            label to set the instruction for
	 * @param instruction
	 *            corresponding instruction
	 */
	public static void setInstruction(final Label label,
			final Instruction instruction) {
		create(label).instruction = instruction;
	}

	/**
	 * Returns the corresponding instruction for the given label if one has been
	 * defined.
	 *
	 * @param label
	 *            label to look for
	 * @return corresponding instruction or <code>null</code>
	 */
	public static Instruction getInstruction(final Label label) {
		final LabelInfo info = get(label);
		return info == null ? null : info.instruction;
	}

	private static LabelInfo get(final Label label) {
		final Object info = label.info;
		return info instanceof LabelInfo ? (LabelInfo) info : null;
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
