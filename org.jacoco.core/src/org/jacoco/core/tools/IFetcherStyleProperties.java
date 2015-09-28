/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.tools;


/**
 * Methods that a consumer of ICoverageFetcherStyle must implement.
 */
public interface IFetcherStyleProperties {

	/**
	 * Returns the Empirical EBigO Attribute used for X-Axis. If {@code null},
	 * the value will default to
	 * {@code WorkloadAttributeMapBuilder.DEFAULT_ATTRIBUTE}
	 * 
	 * @return the Empirical EBigO Attribute used for X-Axis
	 */
	String getEBigOAttribute();

	/**
	 * Returns if EBigO mode of analysis is required.
	 * 
	 * @return {@code true} if EBigO mode of analysis is required.
	 */
	boolean isEBigOEnabled();

}
