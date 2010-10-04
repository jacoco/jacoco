/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.data;

/**
 * Interface for data output of the internal class structure. This interface is
 * meant to be implemented by parties that want to retrieve data from the class
 * analyzing process.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public interface IStructureVisitor {

	/**
	 * Provides structural information about a class as collected during
	 * instrumentation.
	 * 
	 * @param id
	 *            unique id for the class
	 * @return call-back for structure details about the class
	 * 
	 */
	public IClassStructureVisitor visitClassStructure(long id);

}
