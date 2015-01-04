/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
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
 * Interface for hierarchical coverage data nodes with different coverage
 * counters.
 */
public interface ICoverageNode {

	/**
	 * Type of a Java element represented by a {@link ICoverageNode} instance.
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

		/** Bundle of Packages */
		BUNDLE,

		/** Logical Group of Bundles */
		GROUP,

	}

	/**
	 * Different counter types supported by JaCoCo.
	 */
	public enum CounterEntity {

		/** Counter for instructions */
		INSTRUCTION,

		/** Counter for branches */
		BRANCH,

		/** Counter for source lines */
		LINE,

		/** Counter for cyclomatic complexity */
		COMPLEXITY,

		/** Counter for methods */
		METHOD,

		/** Counter for classes */
		CLASS
	}

	/**
	 * Returns the type of element represented by this node.
	 * 
	 * @return type of this node
	 */
	public abstract ElementType getElementType();

	/**
	 * Returns the name of this node.
	 * 
	 * @return name of this node
	 */
	public String getName();

	/**
	 * Returns the counter for byte code instructions.
	 * 
	 * @return counter for instructions
	 */
	public abstract ICounter getInstructionCounter();

	/**
	 * Returns the counter for branches.
	 * 
	 * @return counter for branches
	 */
	public ICounter getBranchCounter();

	/**
	 * Returns the counter for lines.
	 * 
	 * @return counter for lines
	 */
	public ICounter getLineCounter();

	/**
	 * Returns the counter for cyclomatic complexity.
	 * 
	 * @return counter for complexity
	 */
	public ICounter getComplexityCounter();

	/**
	 * Returns the counter for methods.
	 * 
	 * @return counter for methods
	 */
	public ICounter getMethodCounter();

	/**
	 * Returns the counter for classes.
	 * 
	 * @return counter for classes
	 */
	public ICounter getClassCounter();

	/**
	 * Generic access to the the counters.
	 * 
	 * @param entity
	 *            entity we're we want to have the counter for
	 * @return counter for the given entity
	 */
	public ICounter getCounter(CounterEntity entity);

	/**
	 * Creates a plain copy of this node. While {@link ICoverageNode}
	 * implementations may contain heavy data structures, the copy returned by
	 * this method is reduced to the counters only. This helps to save memory
	 * while processing huge structures.
	 * 
	 * @return copy with counters only
	 */
	public ICoverageNode getPlainCopy();

}