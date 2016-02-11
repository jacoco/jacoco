/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.ebigo.internal.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.Assert;

import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.junit.Test;

public class ClassCoverageIteratorsTest {

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNullArg() {
		new ClassCoverageSetIterator(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithEmptyList() {
		new ClassCoverageSetIterator(new ArrayList<CoverageBuilder>());
	}

	private void assertNextThrowsNoSuchElementException(
			ClassCoverageSetIterator instance) {
		try {
			instance.next();
			Assert.fail("Failed to throw NoSuchElementException");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testOneCoverageBuilder_Empty() {
		List<CoverageBuilder> list = new ArrayList<CoverageBuilder>();
		list.add(new CoverageBuilder());

		ClassCoverageSetIterator instance = new ClassCoverageSetIterator(list);

		assertFalse(instance.hasNext());
		assertNextThrowsNoSuchElementException(instance);
	}

	@Test
	public void testOneCoverageBuilder_OneClass() {
		List<CoverageBuilder> list = new ArrayList<CoverageBuilder>();
		CoverageBuilder builder = new CoverageBuilder();
		addClassCoverage(123L, "Sample", false, builder);

		list.add(builder);

		ClassCoverageSetIterator instance = new ClassCoverageSetIterator(list);

		assertTrue(instance.hasNext());
		IClassCoverage[] coverages = instance.next();
		assertEquals(1, coverages.length);
		assertEquals("Sample", coverages[0].getName());
		assertFalse(instance.hasNext());
		assertNextThrowsNoSuchElementException(instance);
	}

	@Test
	public void testOneCoverageBuilder_TwoClasses() {
		List<CoverageBuilder> list = new ArrayList<CoverageBuilder>();
		CoverageBuilder builder = new CoverageBuilder();
		addClassCoverage(123L, "Sample1", false, builder);
		addClassCoverage(124L, "Sample2", false, builder);

		list.add(builder);

		ClassCoverageSetIterator instance = new ClassCoverageSetIterator(list);

		assertTrue(instance.hasNext());
		IClassCoverage[] coverages = instance.next();
		assertEquals(1, coverages.length);
		assertEquals("Sample1", coverages[0].getName());

		assertTrue(instance.hasNext());
		coverages = instance.next();
		assertEquals(1, coverages.length);
		assertEquals("Sample2", coverages[0].getName());

		assertFalse(instance.hasNext());
		assertNextThrowsNoSuchElementException(instance);
	}

	@Test
	public void testTwoCoverageBuilder_MismatchClasses() {
		List<CoverageBuilder> list = new ArrayList<CoverageBuilder>();
		CoverageBuilder builder1 = new CoverageBuilder();
		addClassCoverage(123L, "Sample1", false, builder1);
		list.add(builder1);

		CoverageBuilder builder2 = new CoverageBuilder();
		addClassCoverage(124L, "Sample2", false, builder2);
		list.add(builder2);

		ClassCoverageSetIterator instance = new ClassCoverageSetIterator(list);

		assertFalse(instance.hasNext());
		assertNextThrowsNoSuchElementException(instance);
	}

	@Test
	public void testTwoCoverageBuilder_threeClassPartialMatch() {
		List<CoverageBuilder> list = new ArrayList<CoverageBuilder>();
		CoverageBuilder builder1 = new CoverageBuilder();
		addClassCoverage(123L, "Sample1", false, builder1);
		addClassCoverage(123L, "Sample2", false, builder1);
		addClassCoverage(123L, "Sample3", false, builder1);
		list.add(builder1);

		CoverageBuilder builder2 = new CoverageBuilder();
		addClassCoverage(124L, "Sample1", false, builder2);
		addClassCoverage(124L, "Sample3", false, builder2);
		list.add(builder2);

		ClassCoverageSetIterator instance = new ClassCoverageSetIterator(list);

		assertTrue(instance.hasNext());
		IClassCoverage[] coverages = instance.next();
		assertEquals(2, coverages.length);
		assertEquals("Sample1", coverages[0].getName());

		assertTrue(instance.hasNext());
		coverages = instance.next();
		assertEquals(2, coverages.length);
		assertEquals("Sample3", coverages[0].getName());

		assertFalse(instance.hasNext());
		assertNextThrowsNoSuchElementException(instance);
	}

	@Test
	public void testTwoCoverageBuilder_threeClassPartialMatch2() {
		List<CoverageBuilder> list = new ArrayList<CoverageBuilder>();
		CoverageBuilder builder1 = new CoverageBuilder();
		addClassCoverage(123L, "Sample1", false, builder1);
		addClassCoverage(123L, "Sample2", false, builder1);
		list.add(builder1);

		CoverageBuilder builder2 = new CoverageBuilder();
		addClassCoverage(124L, "Sample1", false, builder2);
		addClassCoverage(124L, "Sample3", false, builder2);
		list.add(builder2);

		ClassCoverageSetIterator instance = new ClassCoverageSetIterator(list);

		assertTrue(instance.hasNext());
		IClassCoverage[] coverages = instance.next();
		assertEquals(2, coverages.length);
		assertEquals("Sample1", coverages[0].getName());

		assertFalse(instance.hasNext());
		assertNextThrowsNoSuchElementException(instance);
	}

	private void addClassCoverage(long id, String name, boolean nomatch,
			CoverageBuilder builder) {
		final ClassCoverageImpl coverage = new ClassCoverageImpl(name, id,
				nomatch);
		coverage.increment(CounterImpl.COUNTER_1_0, CounterImpl.COUNTER_0_0, 6);
		coverage.setSourceFileName(null);
		builder.visitCoverage(coverage);
	}
}