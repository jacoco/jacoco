/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.core.analysis;

import java.util.Collection;

/**
 * Common interface for hierarchical data nodes that have a name, a type and
 * that hold a list of its children.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: 174 $
 */
public interface ICoverageDataNode extends ICoverageDataSummary {

	/**
	 * Type of a Java element represented by a {@link ICoverageDataNode}
	 * instance.
	 */
	public enum ElementType {

		/** Method */
		METHOD,

		/** Class */
		CLASS,

		/** Source File */
		SOURCEFILE,

		/** Java Package */
		PACKAGE,

		/** Custom Node */
		CUSTOM

	}

	/**
	 * Returns the type of element represented by this node.
	 * 
	 * @return type of this node
	 */
	public abstract ElementType getElementType();

	/**
	 * Returns the name of this node. Depending on the kind of node this might
	 * be <code>null</code>.
	 * 
	 * @return name or <code>null</code>
	 */
	public String getName();

	/**
	 * Returns the child elements contained in this node.
	 * 
	 * @return child elements
	 */
	public Collection<ICoverageDataNode> getChilden();

	/**
	 * Returns the line coverage information if this element supports it.
	 * 
	 * @return line coverage or <code>null</code>
	 */
	public ILines getLines();

}
