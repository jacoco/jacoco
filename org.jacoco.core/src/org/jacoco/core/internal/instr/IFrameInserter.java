/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
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

/**
 * Internal interface for insertion of additional stackmap frame in the
 * instruction sequence of a method.
 */
interface IFrameInserter {

	/**
	 * Empty implementation.
	 */
	static final IFrameInserter NOP = new IFrameInserter() {
		public void insertFrame() {
		}
	};

	/**
	 * Inserts an additional frame reflecting the current locals and stack
	 * types.
	 */
	void insertFrame();

}
