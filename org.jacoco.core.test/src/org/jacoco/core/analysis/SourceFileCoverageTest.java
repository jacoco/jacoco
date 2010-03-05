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
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.analysis;

import static org.jacoco.core.analysis.ICoverageNode.ElementType.SOURCEFILE;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for {@link SourceFileCoverage}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class SourceFileCoverageTest {

	@Test
	public void testProperties() {
		SourceFileCoverage data = new SourceFileCoverage("Sample.java",
				"org/jacoco/examples");
		assertEquals(SOURCEFILE, data.getElementType());
		assertEquals("org/jacoco/examples", data.getPackageName());
	}

}
