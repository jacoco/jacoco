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
package org.jacoco.core.analysis;

import java.util.Collection;

/**
 * Coverage data of a bundle. A bundle groups a collection of packages.
 *
 * @see IPackageCoverage
 */
public interface IBundleCoverage extends ICoverageNode {

	/**
	 * Returns all packages contained in this bundle.
	 *
	 * @return all packages
	 */
	Collection<IPackageCoverage> getPackages();

}
