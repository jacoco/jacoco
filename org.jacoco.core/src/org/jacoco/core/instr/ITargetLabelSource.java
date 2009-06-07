/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.instr;

import org.objectweb.asm.Label;

/**
 * Call-back interface to find labels used as control flow targets.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface ITargetLabelSource {

	/**
	 * Checks whether the given label is control flow target (jump, switch,
	 * try/catch etc).
	 * 
	 * @param label
	 *            label to check
	 * @return <code>true</code>, if the label is a control flow target
	 */
	public boolean isTargetLabel(Label label);

}
