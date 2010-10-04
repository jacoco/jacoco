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
package org.jacoco.core.instr;

/**
 * Internal interface to create probe ids unique within a class.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
interface IProbeIdGenerator {

	/**
	 * Returns the next unique probe id.
	 * 
	 * @return unique probe id
	 */
	int nextId();

}
