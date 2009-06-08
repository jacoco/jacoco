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
package org.jacoco.core.analysis;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for {@link BlockNode}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class BlockCoverageDataTest {

	@Test
	public void testGetType() {
		ICoverageDataNode data = new BlockNode(0, new int[0], false);
		assertEquals(ICoverageDataNode.ElementType.BLOCK, data.getElementType());
	}

	@Test
	public void testNotCovered() {
		ICoverageDataNode data = new BlockNode(15, new int[0], false);
		assertEquals(15, data.getInstructionCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getInstructionCounter().getCoveredCount(), 0.0);
		assertEquals(1, data.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(0, data.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(0, data.getClassCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getClassCounter().getCoveredCount(), 0.0);
	}

	@Test
	public void testCovered() {
		ICoverageDataNode data = new BlockNode(15, new int[0], true);
		assertEquals(15, data.getInstructionCounter().getTotalCount(), 0.0);
		assertEquals(15, data.getInstructionCounter().getCoveredCount(), 0.0);
		assertEquals(1, data.getBlockCounter().getTotalCount(), 0.0);
		assertEquals(1, data.getBlockCounter().getCoveredCount(), 0.0);
		assertEquals(0, data.getMethodCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getMethodCounter().getCoveredCount(), 0.0);
		assertEquals(0, data.getClassCounter().getTotalCount(), 0.0);
		assertEquals(0, data.getClassCounter().getCoveredCount(), 0.0);
	}

}
