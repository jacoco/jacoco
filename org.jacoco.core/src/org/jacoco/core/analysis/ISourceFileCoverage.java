/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
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

/**
 * Coverage data of a single source file. As source file contains one or more
 * classes.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public interface ISourceFileCoverage extends ISourceNode {

	/**
	 * Returns the vm name of the package the source file belongs to.
	 * 
	 * @return package name
	 */
	public String getPackageName();

}