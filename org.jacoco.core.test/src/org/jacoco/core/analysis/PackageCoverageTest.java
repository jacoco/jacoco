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
				.singleton(new ClassCoverage("org/jacoco/test/Sample", null,
						"java/lang/Object", new String[0], "Sample.java",
						methods));
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

	@Test
	public void testCountersWithSources() {
		Collection<MethodCoverage> methods = Collections.emptySet();
		// Classes with source reference will not considered for counters:
		final ClassCoverage classnode = new ClassCoverage(
				"org/jacoco/test/Sample", null, "java/lang/Object",
				new String[0], "Sample.java", methods) {
			{
				classCounter = CounterImpl.getInstance(9, 0);
				methodCounter = CounterImpl.getInstance(9, 0);
				blockCounter = CounterImpl.getInstance(9, 0);
				instructionCounter = CounterImpl.getInstance(9, 0);
				lines.increment(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, false);
			}
		};
		// Only source files will be considered for counters:
		final SourceFileCoverage sourceFile = new SourceFileCoverage(
				"Sample.java", "org/jacoco/test/Sample") {
			{
				classCounter = CounterImpl.getInstance(1, 0);
				methodCounter = CounterImpl.getInstance(2, 0);
				blockCounter = CounterImpl.getInstance(3, 0);
				instructionCounter = CounterImpl.getInstance(4, 0);
				lines.increment(new int[] { 1, 2, 3, 4, 5 }, false);
			}
		};
		PackageCoverage data = new PackageCoverage("org/jacoco/test",
				Collections.singleton(classnode), Collections
						.singleton(sourceFile));
		assertEquals(CounterImpl.getInstance(1, 0), data.getClassCounter());
		assertEquals(CounterImpl.getInstance(2, 0), data.getMethodCounter());
		assertEquals(CounterImpl.getInstance(3, 0), data.getBlockCounter());
		assertEquals(CounterImpl.getInstance(4, 0), data
				.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(5, 0), data.getLineCounter());
	}

	@Test
	public void testCountersWithoutSources() {
		Collection<MethodCoverage> methods = Collections.emptySet();
		// Classes without source reference will be considered for counters:
		final ClassCoverage classnode = new ClassCoverage(
				"org/jacoco/test/Sample", null, "java/lang/Object",
				new String[0], null, methods) {
			{
				classCounter = CounterImpl.getInstance(1, 0);
				methodCounter = CounterImpl.getInstance(2, 0);
				blockCounter = CounterImpl.getInstance(3, 0);
				instructionCounter = CounterImpl.getInstance(4, 0);
			}
		};
		final Collection<SourceFileCoverage> sourceFiles = Collections
				.emptySet();
		PackageCoverage data = new PackageCoverage("org/jacoco/test",
				Collections.singleton(classnode), sourceFiles);
		assertEquals(CounterImpl.getInstance(1, 0), data.getClassCounter());
		assertEquals(CounterImpl.getInstance(2, 0), data.getMethodCounter());
		assertEquals(CounterImpl.getInstance(3, 0), data.getBlockCounter());
		assertEquals(CounterImpl.getInstance(4, 0), data
				.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(0, 0), data.getLineCounter());
	}

}
