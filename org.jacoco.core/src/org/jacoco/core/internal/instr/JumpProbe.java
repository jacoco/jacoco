/*******************************************************************************
 * Copyright (c) Copyright (c) Copyright (c) 2009, 2012 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import org.jacoco.core.internal.flow.LabelInfo;
import org.objectweb.asm.Label;

/**
 * Probe to be inserted into a control flow edge along a jump instruction. This
 * is implemented by replacing the original jump target by an intermediate
 * target followed by the probe and a GOTO instruction back to the original
 * target. This internal data structure stores all required information to
 * append such jump probes at the end of a method.
 */
class JumpProbe {

	private final Label target;

	private final int probeid;

	private final Label intermediate;

	JumpProbe(final Label target, final int probeid) {
		this.intermediate = new Label();
		this.target = target;
		this.probeid = probeid;
		LabelInfo.setIntermediateLabel(target, this.intermediate);
	}

	JumpProbe(final Label target) {
		this(target, LabelInfo.getProbeId(target));
	}

	/**
	 * Returns the original jump target.
	 * 
	 * @return original jump target
	 */
	public Label getTarget() {
		return target;
	}

	/**
	 * Returns the corresponding probe id.
	 * 
	 * @return corresponding probe id
	 */
	public int getProbeId() {
		return probeid;
	}

	/**
	 * Returns the label of the intermediate jump target
	 * 
	 * @return intermediate jump target
	 */
	public Label getIntermediate() {
		return intermediate;
	}

}
