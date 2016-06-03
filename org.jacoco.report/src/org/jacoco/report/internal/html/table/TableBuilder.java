/*******************************************************************************
 * Copyright (c) 2009, 2017 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.internal.html.table;

import java.util.Locale;

import org.jacoco.core.analysis.ICoverageNode.CounterEntity;

/**
 * Builds a {@link Table} based on a string definition of its columns.
 */
public class TableBuilder {

	private final Locale locale;

	/**
	 * New builder using the given locale.
	 * 
	 * @param locale
	 *            locale used to render numeric values
	 */
	public TableBuilder(final Locale locale) {
		this.locale = locale;
	}

	/**
	 * Build a new {@link Table} instance.
	 * 
	 * @param definition
	 *            string definition of the table's columns
	 * @return new table
	 */
	public Table build(final String definition) {
		final StringParser parser = new StringParser(definition);
		final Table table = new Table();
		while (parser.hasNext()) {
			column(parser, table);
		}
		return table;
	}

	private void column(final StringParser parser, final Table table) {
		final boolean separator = parser.isNext('|');
		final IColumnRenderer renderer = entity(parser);
		final boolean defaultSorting = parser.isNext('^');
		parser.expectNext('[');
		final String header = parser.read(']');
		table.add(header, renderer.getStyle(separator), renderer,
				defaultSorting);
	}

	private IColumnRenderer entity(final StringParser parser) {
		final char entityChar = parser.getNext();
		switch (entityChar) {
		case 'E':
			return new LabelColumn();
		case 'I':
			return representation(CounterEntity.INSTRUCTION, parser);
		case 'B':
			return representation(CounterEntity.BRANCH, parser);
		case 'X':
			return representation(CounterEntity.COMPLEXITY, parser);
		case 'L':
			return representation(CounterEntity.LINE, parser);
		case 'M':
			return representation(CounterEntity.METHOD, parser);
		case 'C':
			return representation(CounterEntity.CLASS, parser);
		}
		throw new IllegalArgumentException(
				"Unknown entity type: " + entityChar);
	}

	private IColumnRenderer representation(final CounterEntity entity,
			final StringParser parser) {
		final char repr = parser.getNext();
		switch (repr) {
		case 'm':
			return CounterColumn.newMissed(entity, locale);
		case 'c':
			return CounterColumn.newCovered(entity, locale);
		case 't':
			return CounterColumn.newTotal(entity, locale);
		case 'p':
			return new PercentageColumn(entity, locale);
		case 'b':
			return new BarColumn(entity, locale);
		}
		throw new IllegalArgumentException(
				"Unknown representation type: " + repr);
	}
}
