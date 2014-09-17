/*******************************************************************************
 * Copyright (c) 2009, 2014 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.check;

import org.jacoco.core.analysis.ICoverageNode;

/**
 * Call-back interface which is used to report rule violations / conformance to.
 * 
 */
public interface ICheckerOutput {

	/**
	 * Called for every rule violation.
	 * 
	 * @param node
	 *            node which violates a rule
	 * @param rule
	 *            rule which is violated
	 * @param limit
	 *            limit which is violated
	 * @param message
	 *            readable message describing this violation
	 */
	void onViolation(ICoverageNode node, Rule rule, Limit limit, String message);

    /**
     * Called for every conformant rule
     *
     * @param node
     *            node which conforms to a rule
     * @param rule
     *            rule which has been checked
     * @param limit
     *            limit which is upheld
     * @param message
     *            readable message describing the checked rule
     */
    void onConformance(ICoverageNode node, Rule rule, Limit limit, String message);

}
