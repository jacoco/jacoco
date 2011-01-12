/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.test.validation;

import static org.jacoco.core.analysis.ILine.PARTLY_COVERED;

import org.jacoco.core.test.validation.targets.Target10;
import org.junit.Test;

/**
 * Test of blocks before the super constructor call.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class BlocksBeforeSuperConstructorTest extends ValidationTestBase {

	public BlocksBeforeSuperConstructorTest() {
		super(Target10.class);
	}

	@Override
	protected void run(final Class<?> targetClass) throws Exception {
		targetClass.newInstance();
	}

	@Test
	public void testCoverageResult() {

		assertLine("super", PARTLY_COVERED, 1, 1);

	}

}
