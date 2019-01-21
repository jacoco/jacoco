/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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
 * Call-back interface which is used to report rule violations to.
 * 
 */
public interface IViolationsOutput {

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

}
