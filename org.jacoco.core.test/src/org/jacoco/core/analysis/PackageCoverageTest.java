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
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

/**
 * Unit test for {@link PackageCoverage}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class PackageCoverageTest {

	@Test
	public void testProperties() {
		Collection<MethodCoverage> methods = Collections.emptySet();
		Collection<ClassCoverage> classes = Collections
				.singleton(new ClassCoverage("org/jacoco/test/Sample",
						"Sample.java", methods));
		Collection<SourceFileCoverage> sourceFiles = Collections
				.singleton(new SourceFileCoverage("Sample.java",
						"org/jacoco/test/Sample"));
		PackageCoverage data = new PackageCoverage("org/jacoco/test", classes,
				sourceFiles);
		assertEquals(ICoverageNode.ElementType.PACKAGE, data.getElementType());
		assertEquals("org/jacoco/test", data.getName());
		assertEquals(classes, data.getClasses());
		assertEquals(sourceFiles, data.getSourceFiles());
		assertNull(data.getLines());
	}

}
