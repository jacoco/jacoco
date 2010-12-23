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
package org.jacoco.core.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

/**
 * Unit tests for {@link BundleCoverage}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class BundleCoverageTest {

	@Test
	public void testProperties() {
		Collection<ClassCoverage> classes = Collections.emptySet();
		Collection<SourceFileCoverage> sourcefiles = Collections.emptySet();
		Collection<PackageCoverage> packages = Collections
				.singleton(new PackageCoverage("p1", classes, sourcefiles));
		BundleCoverage bundle = new BundleCoverage("testbundle", packages);
		assertEquals(ICoverageNode.ElementType.BUNDLE, bundle.getElementType());
		assertEquals("testbundle", bundle.getName());
		assertEquals(packages, bundle.getPackages());
	}

	@Test
	public void testCounters() {
		Collection<ClassCoverage> classes = Collections.emptySet();
		Collection<SourceFileCoverage> sourcefiles = Collections.emptySet();
		final PackageCoverage p1 = new PackageCoverage("p1", classes,
				sourcefiles) {
			{
				classCounter = CounterImpl.getInstance(1, 0);
				methodCounter = CounterImpl.getInstance(2, 0);
				branchCounter = CounterImpl.getInstance(3, 0);
				instructionCounter = CounterImpl.getInstance(4, 0);
				lineCounter = CounterImpl.getInstance(5, 0);
			}
		};
		final PackageCoverage p2 = new PackageCoverage("p1", classes,
				sourcefiles) {
			{
				classCounter = CounterImpl.getInstance(1, 0);
				methodCounter = CounterImpl.getInstance(2, 0);
				branchCounter = CounterImpl.getInstance(3, 0);
				instructionCounter = CounterImpl.getInstance(4, 0);
				lineCounter = CounterImpl.getInstance(5, 0);
			}
		};
		BundleCoverage bundle = new BundleCoverage("testbundle", Arrays.asList(
				p1, p2));
		assertEquals(CounterImpl.getInstance(2, 0), bundle.getClassCounter());
		assertEquals(CounterImpl.getInstance(4, 0), bundle.getMethodCounter());
		assertEquals(CounterImpl.getInstance(6, 0), bundle.getBranchCounter());
		assertEquals(CounterImpl.getInstance(8, 0),
				bundle.getInstructionCounter());
		assertEquals(CounterImpl.getInstance(10, 0), bundle.getLineCounter());
	}

	@Test
	public void testGroupByPackage() {
		ClassCoverage ca = new ClassCoverage("p1/A", 1, null,
				"java/lang/Object", new String[0]);
		ca.setSourceFileName("A.java");
		ClassCoverage cb = new ClassCoverage("p2/B", 2, null,
				"java/lang/Object", new String[0]);
		cb.setSourceFileName("B.java");
		SourceFileCoverage sb = new SourceFileCoverage("B.java", "p2");
		SourceFileCoverage sc = new SourceFileCoverage("C.java", "p3");
		BundleCoverage bundle = new BundleCoverage("bundle", Arrays.asList(ca,
				cb), Arrays.asList(sb, sc));

		Collection<PackageCoverage> packages = bundle.getPackages();
		assertEquals(3, packages.size(), 0.0);

		PackageCoverage p1 = findPackage("p1", packages);
		assertNotNull(p1);
		assertEquals(Collections.singletonList(ca), p1.getClasses());
		assertTrue(p1.getSourceFiles().isEmpty());

		PackageCoverage p2 = findPackage("p2", packages);
		assertNotNull(p2);
		assertEquals(Collections.singletonList(cb), p2.getClasses());
		assertEquals(Collections.singletonList(sb), p2.getSourceFiles());

		PackageCoverage p3 = findPackage("p3", packages);
		assertNotNull(p3);
		assertTrue(p3.getClasses().isEmpty());
		assertEquals(Collections.singletonList(sc), p3.getSourceFiles());
	}

	private PackageCoverage findPackage(String name,
			Collection<PackageCoverage> packages) {
		for (PackageCoverage p : packages) {
			if (name.equals(p.getName())) {
				return p;
			}
		}
		return null;
	}

}
