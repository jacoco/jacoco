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
package org.jacoco.ant;

import org.jacoco.core.tools.DefaultCoverageFetcherStyle;
import org.jacoco.core.tools.ICoverageFetcherStyle;
import org.jacoco.core.tools.IFetcherStyleProperties;
import org.jacoco.ebigo.tools.EBigOCoverageFetcherStyle;

/**
 * The base class for different methods of fetching execution, session, and
 * coverage data.
 */
public final class AntCoverageFetcherFactory {
	private AntCoverageFetcherFactory() {
	}

	/**
	 * A factory to construct a new concrete instance of this class.
	 * 
	 * @param properties
	 *            provides the properties used to determine which concrete
	 *            instance to construct.
	 * @return a new concrete instance of this class.
	 */
	public static final ICoverageFetcherStyle newFetcher(
			final IFetcherStyleProperties properties) {
		final ICoverageFetcherStyle dataFetcher = !properties.isEBigOEnabled() ? new DefaultCoverageFetcherStyle(
				properties) : new EBigOCoverageFetcherStyle(properties);
		return dataFetcher;
	}
}