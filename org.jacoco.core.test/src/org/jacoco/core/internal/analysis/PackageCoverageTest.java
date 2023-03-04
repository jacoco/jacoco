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
package org.jacoco.core.internal.analysis;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.junit.Test;

/**
 * Unit test for {@link PackageCoverageImpl}.
 */
public class PackageCoverageTest {

	@Test
	public void testProperties() {
		Collection<IClassCoverage> classes = Collections.singleton(
				(IClassCoverage) new ClassCoverageImpl("org/jacoco/test/Sample",
						0, false));
		Collection<ISourceFileCoverage> sourceFiles = Collections.singleton(
				(ISourceFileCoverage) new SourceFileCoverageImpl("Sample.java",
						"org/jacoco/test/Sample"));
		PackageCoverageImpl data = new PackageCoverageImpl("org/jacoco/test",
				classes, sourceFiles);
		assertEquals(ICoverageNode.ElementType.PACKAGE, data.getElementType());
		assertEquals("org/jacoco/test", data.getName());
		assertEquals(classes, data.getClasses());
		assertEquals(sourceFiles, data.getSourceFiles());
	}

	@Test
	public void testCountersWithSources() {
		// Classes with source reference will not considered for counters:
		final ClassCoverageImpl classnode = new ClassCoverageImpl(
				"org/jacoco/test/Sample", 0, false) {
			{
				classCounter = CounterImpl.getInstance(9, 0);
				methodCounter = CounterImpl.getInstance(9, 0);
				branchCounter = CounterImpl.getInstance(9, 0);
				instructionCounter = CounterImpl.getInstance(9, 0);
			}
		};
		classnode.setSourceFileName("Sample.java");
		// Only source files will be considered for counters:
		final ISourceFileCoverage sourceFile = new SourceFileCoverageImpl(
				"Sample.java", "org/jacoco/test/Sample") {
			{
				classCounter = CounterImpl.getInstance(1, 0);
				methodCounter = CounterImpl.getInstance(2, 0);
				branchCounter = CounterImpl.getInstance(3, 0);
				instructionCounter = CounterImpl.getInstance(4, 0);
			}
		};
		PackageCoverageImpl data = new PackageCoverageImpl("org/jacoco/test",
				Collections.singleton((IClassCoverage) classnode),
				Collections.singleton(sourceFile));
		assertEquals(CounterImpl.getInstance(1, 0), data.getClassCounter());
		assertEquals(CounterImpl.getInstance(2, 0), data.getMethodCounter());
		assertEquals(CounterImpl.getInstance(3, 0), data.getBranchCounter());
		assertEquals(CounterImpl.getInstance(4, 0),
				data.getInstructionCounter());
	}

	@Test
	public void testCountersWithoutSources() {
		// Classes without source reference will be considered for counters:
		final ClassCoverageImpl classnode = new ClassCoverageImpl(
				"org/jacoco/test/Sample", 0, false) {
			{
				classCounter = CounterImpl.getInstance(1, 0);
				methodCounter = CounterImpl.getInstance(2, 0);
				branchCounter = CounterImpl.getInstance(3, 0);
				instructionCounter = CounterImpl.getInstance(4, 0);
			}
		};
		final Collection<ISourceFileCoverage> sourceFiles = Collections
				.emptySet();
		PackageCoverageImpl data = new PackageCoverageImpl("org/jacoco/test",
				Collections.singleton((IClassCoverage) classnode), sourceFiles);
		assertEquals(CounterImpl.getInstance(1, 0), data.getClassCounter());
		assertEquals(CounterImpl.getInstance(2, 0), data.getMethodCounter());
		assertEquals(CounterImpl.getInstance(3, 0), data.getBranchCounter());
		assertEquals(CounterImpl.getInstance(4, 0),
				data.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(0, 0), data.getLineCounter());
	}

}
