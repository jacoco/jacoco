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
package org.jacoco.report.internal.html.table;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link SortIndex}.
 */
public class SortIndexTest {

	private SortIndex<Integer> index;

	@Before
	public void setup() {
		index = new SortIndex<Integer>(new Comparator<Integer>() {
			public int compare(Integer i1, Integer i2) {
				return i1.compareTo(i2);
			}
		});
	}

	@Test
	public void testEmptyList() {
		index.init(Arrays.<Integer> asList());
	}

	@Test
	public void testSingleton() {
		final List<Integer> list = createList(1);
		index.init(list);
		assertSequence(list);
	}

	@Test
	public void testSorted() {
		final List<Integer> list = createList(20);
		index.init(list);
		assertSequence(list);
	}

	@Test
	public void testIncreaseBuffer() {
		index.init(createList(15));
		final List<Integer> list = createList(20);
		index.init(list);
		assertSequence(list);
	}

	@Test
	public void testReverse() {
		final List<Integer> list = createList(57);
		Collections.reverse(list);
		index.init(list);
		assertSequence(list);
	}

	@Test
	public void testShuffle() {
		final List<Integer> list = createList(71);
		Collections.shuffle(list);
		index.init(list);
		assertSequence(list);
	}

	private List<Integer> createList(int length) {
		List<Integer> list = new ArrayList<Integer>(length);
		for (int i = 0; i < length; i++) {
			list.add(Integer.valueOf(i));
		}
		return list;
	}

	private void assertSequence(List<Integer> list) {
		int idx = 0;
		for (Integer i : list) {
			assertEquals(i.intValue(), index.getPosition(idx++));
		}
	}

}
