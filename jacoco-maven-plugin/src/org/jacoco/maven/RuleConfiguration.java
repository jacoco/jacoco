/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *    Kyle Lieber - implementation of CheckMojo
 *    Marc Hoffmann - redesign using report APIs
 *
 *******************************************************************************/
package org.jacoco.maven;

import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.check.Limit;
import org.jacoco.report.check.Rule;

/**
 * Wrapper for {@link Rule} objects to allow Maven style includes/excludes lists
 *
 */
public class RuleConfiguration {

	final Rule rule;

	/**
	 * Create a new configuration instance.
	 */
	public RuleConfiguration() {
		rule = new Rule();
	}

	/**
	 * @param element
	 *            element type this rule applies to TODO: use ElementType
	 *            directly once Maven 3 is required.
	 */
	public void setElement(final String element) {
		rule.setElement(ElementType.valueOf(element));
	}

	/**
	 * @param includes
	 *            includes patterns
	 */
	public void setIncludes(final List<String> includes) {
		rule.setIncludes(StringUtils.join(includes.iterator(), ":"));
	}

	/**
	 *
	 * @param excludes
	 *            excludes patterns
	 */
	public void setExcludes(final List<String> excludes) {
		rule.setExcludes(StringUtils.join(excludes.iterator(), ":"));
	}

	/**
	 * @param limits
	 *            list of {@link Limit}s configured for this rule
	 */
	public void setLimits(final List<Limit> limits) {
		rule.setLimits(limits);
	}

}
