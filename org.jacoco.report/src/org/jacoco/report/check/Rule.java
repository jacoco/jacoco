/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.check;

import java.util.ArrayList;
import java.util.List;

import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.runtime.WildcardMatcher;

/**
 * A rule applies for a certain element type and can define any number of limits
 * for all elements of this type.
 */
public final class Rule {

	private ElementType element;
	private String includes;
	private String excludes;
	private List<Limit> limits;

	private WildcardMatcher includesMatcher;
	private WildcardMatcher excludesMatcher;

	/**
	 * Creates a new Rule without limits.
	 */
	public Rule() {
		this.element = ElementType.BUNDLE;
		this.limits = new ArrayList<Limit>();
		this.setIncludes("*");
		this.setExcludes("");
	}

	/**
	 * Returns element type this rule applies to.
	 *
	 * @return element type this rule applies to
	 */
	public ElementType getElement() {
		return element;
	}

	/**
	 * Sets element type this rule applies to.
	 *
	 * @param elementType
	 *            element type this rule applies to
	 */
	public void setElement(final ElementType elementType) {
		this.element = elementType;
	}

	/**
	 * Returns includes pattern.
	 *
	 * @return includes pattern
	 */
	public String getIncludes() {
		return includes;
	}

	/**
	 * Sets includes pattern.
	 *
	 * @param includes
	 *            includes pattern
	 */
	public void setIncludes(final String includes) {
		this.includes = includes;
		this.includesMatcher = new WildcardMatcher(includes);
	}

	/**
	 * Returns excludes pattern.
	 *
	 * @return excludes pattern
	 */
	public String getExcludes() {
		return excludes;
	}

	/**
	 * Sets excludes pattern.
	 *
	 * @param excludes
	 *            excludes patterns
	 */
	public void setExcludes(final String excludes) {
		this.excludes = excludes;
		this.excludesMatcher = new WildcardMatcher(excludes);
	}

	/**
	 * Returns list of {@link Limit}s configured for this rule.
	 *
	 * @return list of {@link Limit}s configured for this rule
	 */
	public List<Limit> getLimits() {
		return limits;
	}

	/**
	 * Sets list of {@link Limit}s configured for this rule.
	 *
	 * @param limits
	 *            list of {@link Limit}s configured for this rule
	 */
	public void setLimits(final List<Limit> limits) {
		this.limits = limits;
	}

	/**
	 * Creates and adds a new {@link Limit}.
	 *
	 * @return creates {@link Limit}
	 */
	public Limit createLimit() {
		final Limit limit = new Limit();
		this.limits.add(limit);
		return limit;
	}

	boolean matches(final String name) {
		return includesMatcher.matches(name) && !excludesMatcher.matches(name);
	}

}
