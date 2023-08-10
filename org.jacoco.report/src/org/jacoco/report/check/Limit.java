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
package org.jacoco.report.check;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.analysis.ICounter.CounterValue;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;

/**
 * Descriptor for a limit which is given by a {@link Rule}.
 */
public class Limit {

	private static final Map<CounterValue, String> VALUE_NAMES;
	private static final Map<CounterEntity, String> ENTITY_NAMES;

	static {
		final Map<CounterValue, String> values = new HashMap<CounterValue, String>();
		values.put(CounterValue.TOTALCOUNT, "total count");
		values.put(CounterValue.MISSEDCOUNT, "missed count");
		values.put(CounterValue.COVEREDCOUNT, "covered count");
		values.put(CounterValue.MISSEDRATIO, "missed ratio");
		values.put(CounterValue.COVEREDRATIO, "covered ratio");
		VALUE_NAMES = Collections.unmodifiableMap(values);

		final Map<CounterEntity, String> entities = new HashMap<CounterEntity, String>();
		entities.put(CounterEntity.INSTRUCTION, "instructions");
		entities.put(CounterEntity.BRANCH, "branches");
		entities.put(CounterEntity.COMPLEXITY, "complexity");
		entities.put(CounterEntity.LINE, "lines");
		entities.put(CounterEntity.METHOD, "methods");
		entities.put(CounterEntity.CLASS, "classes");
		ENTITY_NAMES = Collections.unmodifiableMap(entities);
	}

	private CounterEntity entity;

	private CounterValue value;

	private BigDecimal minimum;

	private BigDecimal maximum;

	/**
	 * Creates a new instance with the following defaults:
	 * <ul>
	 * <li>counter entity: {@link CounterEntity#INSTRUCTION}
	 * <li>counter value: {@link CounterValue#COVEREDRATIO}
	 * <li>minimum: no limit
	 * <li>maximum: no limit
	 * </ul>
	 */
	public Limit() {
		this.entity = CounterEntity.INSTRUCTION;
		this.value = CounterValue.COVEREDRATIO;
	}

	/**
	 * @return the configured counter entity to check
	 */
	public CounterEntity getEntity() {
		return entity;
	}

	/**
	 * Sets the counter entity to check.
	 *
	 * @param entity
	 *            counter entity to check
	 */
	// TODO: use CounterEntity directly once Maven 3 is required.
	public void setCounter(final String entity) {
		this.entity = CounterEntity.valueOf(entity);
	}

	/**
	 * @return the configured value to check
	 */
	public CounterValue getValue() {
		return value;
	}

	/**
	 * Sets the value to check.
	 *
	 * @param value
	 *            value to check
	 */
	// TODO: use CounterValue directly once Maven 3 is required.
	public void setValue(final String value) {
		this.value = CounterValue.valueOf(value);
	}

	/**
	 * @return configured minimum value, or <code>null</code> if no minimum is
	 *         given
	 */
	public String getMinimum() {
		return minimum == null ? null : minimum.toPlainString();
	}

	/**
	 * Sets the expected minimum value. If the minimum refers to a ratio it must
	 * be in the range from 0.0 to 1.0 where the number of decimal places will
	 * also determine the precision in error messages. A limit ratio may
	 * optionally be declared as a percentage where 0.80 and 80% represent the
	 * same value.
	 *
	 * @param minimum
	 *            allowed minimum or <code>null</code>, if no minimum should be
	 *            checked
	 */
	public void setMinimum(final String minimum) {
		this.minimum = parseValue(minimum);
	}

	/**
	 * @return configured maximum value, or <code>null</code> if no maximum is
	 *         given
	 */
	public String getMaximum() {
		return maximum == null ? null : maximum.toPlainString();
	}

	/**
	 * Sets the expected maximum value. If the maximum refers to a ratio it must
	 * be in the range from 0.0 to 1.0 where the number of decimal places will
	 * also determine the precision in error messages. A limit ratio may
	 * optionally be declared as a percentage where 0.80 and 80% represent the
	 * same value.
	 *
	 * @param maximum
	 *            allowed maximum or <code>null</code>, if no maximum should be
	 *            checked
	 */
	public void setMaximum(final String maximum) {
		this.maximum = parseValue(maximum);
	}

	private static BigDecimal parseValue(final String value) {
		if (value == null) {
			return null;
		}

		final String trimmedValue = value.trim();
		if (trimmedValue.endsWith("%")) {
			final String percent = trimmedValue.substring(0,
					trimmedValue.length() - 1);
			return new BigDecimal(percent).movePointLeft(2);
		}

		return new BigDecimal(trimmedValue);
	}

	String check(final ICoverageNode node) {
		final String msg = checkRatioLimit();
		if (msg != null) {
			return msg;
		}
		final double d = node.getCounter(entity).getValue(value);
		if (Double.isNaN(d)) {
			return null;
		}
		final BigDecimal bd = BigDecimal.valueOf(d);
		if (minimum != null && minimum.compareTo(bd) > 0) {
			return message("minimum", bd, minimum, RoundingMode.FLOOR);
		}
		if (maximum != null && maximum.compareTo(bd) < 0) {
			return message("maximum", bd, maximum, RoundingMode.CEILING);
		}
		return null;
	}

	private String message(final String minmax, final BigDecimal v,
			final BigDecimal ref, final RoundingMode mode) {
		final BigDecimal rounded = v.setScale(ref.scale(), mode);
		return String.format("%s %s is %s, but expected %s is %s",
				ENTITY_NAMES.get(entity), VALUE_NAMES.get(value),
				rounded.toPlainString(), minmax, ref.toPlainString());
	}

	private String checkRatioLimit() {
		if (CounterValue.MISSEDRATIO.equals(value)
				|| CounterValue.COVEREDRATIO.equals(value)) {
			final String minmsg = checkRatioLimit("minimum", minimum);
			if (minmsg != null) {
				return minmsg;
			}
			final String maxmsg = checkRatioLimit("maximum", maximum);
			if (maxmsg != null) {
				return maxmsg;
			}
		}
		return null;
	}

	private String checkRatioLimit(final String minmax, final BigDecimal v) {
		if (v != null && (v.compareTo(BigDecimal.ZERO) < 0
				|| v.compareTo(BigDecimal.ONE) > 0)) {
			return String.format(
					"given %s ratio is %s, but must be between 0.0 and 1.0",
					minmax, v);
		}
		return null;
	}

}
