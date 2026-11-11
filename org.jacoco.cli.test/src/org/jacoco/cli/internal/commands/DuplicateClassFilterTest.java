/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    r3bb1t - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.cli.internal.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jacoco.cli.internal.commands.DuplicateClassFilter.Duplicate;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link DuplicateClassFilter}.
 */
public class DuplicateClassFilterTest implements ICoverageVisitor {

	private ExecutionDataStore executionData;

	private DuplicateClassFilter filter;

	private List<IClassCoverage> kept;

	@Before
	public void setup() {
		executionData = new ExecutionDataStore();
		filter = new DuplicateClassFilter(executionData);
		kept = new ArrayList<IClassCoverage>();
	}

	public void visitCoverage(final IClassCoverage coverage) {
		kept.add(coverage);
	}

	@Test
	public void should_pass_all_classes_when_there_are_no_duplicates() {
		final IClassCoverage foo = visit("Foo", 1, "foo");
		final IClassCoverage bar = visit("Bar", 2, "bar");

		filter.accept(this);

		assertEquals(2, kept.size());
		assertTrue(kept.contains(foo));
		assertTrue(kept.contains(bar));
		assertTrue(filter.getDuplicates().isEmpty());
	}

	@Test
	public void should_pass_identical_copies_once_without_duplicate() {
		visit("Foo", 1, "b");
		visit("Foo", 1, "a");

		filter.accept(this);

		assertEquals(1, kept.size());
		assertTrue(filter.getDuplicates().isEmpty());
	}

	@Test
	public void should_keep_copy_with_execution_data_over_lower_class_id() {
		executionData.put(new ExecutionData(2, "Foo", new boolean[0]));
		visit("Foo", 1, "unexecuted");
		final IClassCoverage executed = visit("Foo", 2, "executed");

		filter.accept(this);

		assertEquals(Arrays.asList(executed), kept);
		final Duplicate duplicate = singleDuplicate();
		assertEquals("Foo", duplicate.getName());
		assertEquals("executed", duplicate.getKept());
		assertEquals(Arrays.asList("unexecuted"), duplicate.getIgnored());
		assertEquals(1, duplicate.getExecutedCopies());
	}

	@Test
	public void should_keep_copy_with_lowest_class_id_without_execution_data() {
		visit("Foo", 3, "c");
		final IClassCoverage lowest = visit("Foo", 1, "b");
		visit("Foo", 2, "a");

		filter.accept(this);

		assertEquals(Arrays.asList(lowest), kept);
		final Duplicate duplicate = singleDuplicate();
		assertEquals("b", duplicate.getKept());
		assertEquals(Arrays.asList("a", "c"), duplicate.getIgnored());
		assertEquals(0, duplicate.getExecutedCopies());
	}

	@Test
	public void should_keep_copy_with_lowest_class_id_when_several_copies_have_execution_data() {
		executionData.put(new ExecutionData(1, "Foo", new boolean[0]));
		executionData.put(new ExecutionData(2, "Foo", new boolean[0]));
		visit("Foo", 2, "high");
		final IClassCoverage lowest = visit("Foo", 1, "low");

		filter.accept(this);

		assertEquals(Arrays.asList(lowest), kept);
		final Duplicate duplicate = singleDuplicate();
		assertEquals("low", duplicate.getKept());
		assertEquals(2, duplicate.getExecutedCopies());
	}

	@Test
	public void should_name_lowest_location_of_identical_copies() {
		visit("Foo", 1, "z");
		visit("Foo", 1, "y");
		visit("Foo", 2, "x");

		final Duplicate duplicate = singleDuplicate();
		assertEquals("y", duplicate.getKept());
		assertEquals(Arrays.asList("x"), duplicate.getIgnored());
	}

	@Test
	public void should_sort_duplicates_by_name() {
		visit("Zed", 1, "z1");
		visit("Zed", 2, "z2");
		visit("Abc", 1, "a1");
		visit("Abc", 2, "a2");

		final List<Duplicate> duplicates = filter.getDuplicates();
		assertEquals(2, duplicates.size());
		assertEquals("Abc", duplicates.get(0).getName());
		assertEquals("Zed", duplicates.get(1).getName());
	}

	private IClassCoverage visit(final String name, final long id,
			final String location) {
		final ClassCoverageImpl coverage = new ClassCoverageImpl(name, id,
				false);
		filter.setLocation(location);
		filter.visitCoverage(coverage);
		return coverage;
	}

	private Duplicate singleDuplicate() {
		final List<Duplicate> duplicates = filter.getDuplicates();
		assertEquals(1, duplicates.size());
		return duplicates.get(0);
	}

}
