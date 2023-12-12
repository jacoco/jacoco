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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.junit.Test;

/**
 * Unit tests for {@link BundleCoverageImpl}.
 */
public class BundleCoverageImplTest {

	@Test
	public void testProperties() {
		Collection<IClassCoverage> classes = Collections.emptySet();
		Collection<ISourceFileCoverage> sourcefiles = Collections.emptySet();
		Collection<IPackageCoverage> packages = Collections
				.singleton((IPackageCoverage) new PackageCoverageImpl("p1",
						classes, sourcefiles));
		BundleCoverageImpl bundle = new BundleCoverageImpl("testbundle",
				packages);
		assertEquals(ICoverageNode.ElementType.BUNDLE, bundle.getElementType());
		assertEquals("testbundle", bundle.getName());
		assertEquals(packages, bundle.getPackages());
	}

	@Test
	public void testCounters() {
		Collection<IClassCoverage> classes = Collections.emptySet();
		Collection<ISourceFileCoverage> sourcefiles = Collections.emptySet();
		final IPackageCoverage p1 = new PackageCoverageImpl("p1", classes,
				sourcefiles) {
			{
				classCounter = CounterImpl.getInstance(1, 0);
				methodCounter = CounterImpl.getInstance(2, 0);
				branchCounter = CounterImpl.getInstance(3, 0);
				instructionCounter = CounterImpl.getInstance(4, 0);
				lineCounter = CounterImpl.getInstance(5, 0);
			}
		};
		final IPackageCoverage p2 = new PackageCoverageImpl("p1", classes,
				sourcefiles) {
			{
				classCounter = CounterImpl.getInstance(1, 0);
				methodCounter = CounterImpl.getInstance(2, 0);
				branchCounter = CounterImpl.getInstance(3, 0);
				instructionCounter = CounterImpl.getInstance(4, 0);
				lineCounter = CounterImpl.getInstance(5, 0);
			}
		};
		BundleCoverageImpl bundle = new BundleCoverageImpl("testbundle",
				Arrays.asList(p1, p2));
		assertEquals(CounterImpl.getInstance(2, 0), bundle.getClassCounter());
		assertEquals(CounterImpl.getInstance(4, 0), bundle.getMethodCounter());
		assertEquals(CounterImpl.getInstance(6, 0), bundle.getBranchCounter());
		assertEquals(CounterImpl.getInstance(8, 0),
				bundle.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(10, 0), bundle.getLineCounter());
	}

	@Test
	public void testGroupByPackage() {
		ClassCoverageImpl ca = new ClassCoverageImpl("p1/A", 1, false);
		ca.setSourceFileName("A.java");
		ClassCoverageImpl cb = new ClassCoverageImpl("p2/B", 2, false);
		cb.setSourceFileName("B.java");
		ISourceFileCoverage sb = new SourceFileCoverageImpl("B.java", "p2");
		ISourceFileCoverage sc = new SourceFileCoverageImpl("C.java", "p3");
		BundleCoverageImpl bundle = new BundleCoverageImpl("bundle",
				Arrays.asList((IClassCoverage) ca, (IClassCoverage) cb),
				Arrays.asList(sb, sc));

		Collection<IPackageCoverage> packages = bundle.getPackages();
		assertEquals(3, packages.size(), 0.0);

		IPackageCoverage p1 = findPackage("p1", packages);
		assertNotNull(p1);
		assertEquals(Collections.singletonList(ca), p1.getClasses());
		assertTrue(p1.getSourceFiles().isEmpty());

		IPackageCoverage p2 = findPackage("p2", packages);
		assertNotNull(p2);
		assertEquals(Collections.singletonList(cb), p2.getClasses());
		assertEquals(Collections.singletonList(sb), p2.getSourceFiles());

		IPackageCoverage p3 = findPackage("p3", packages);
		assertNotNull(p3);
		assertTrue(p3.getClasses().isEmpty());
		assertEquals(Collections.singletonList(sc), p3.getSourceFiles());
	}

	private IPackageCoverage findPackage(String name,
			Collection<IPackageCoverage> packages) {
		for (IPackageCoverage p : packages) {
			if (name.equals(p.getName())) {
				return p;
			}
		}
		return null;
	}

}
