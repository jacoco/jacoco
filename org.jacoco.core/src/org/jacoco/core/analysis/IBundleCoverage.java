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
