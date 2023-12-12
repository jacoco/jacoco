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

/**
 * Interface for hierarchical coverage data nodes with different coverage
 * counters.
 */
public interface ICoverageNode {

	/**
	 * Type of a Java element represented by a {@link ICoverageNode} instance.
	 */
	enum ElementType {

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
	enum CounterEntity {

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
	ElementType getElementType();

	/**
	 * Returns the name of this node.
	 *
	 * @return name of this node
	 */
	String getName();

	/**
	 * Returns the counter for byte code instructions.
	 *
	 * @return counter for instructions
	 */
	ICounter getInstructionCounter();

	/**
	 * Returns the counter for branches.
	 *
	 * @return counter for branches
	 */
	ICounter getBranchCounter();

	/**
	 * Returns the counter for lines.
	 *
	 * @return counter for lines
	 */
	ICounter getLineCounter();

	/**
	 * Returns the counter for cyclomatic complexity.
	 *
	 * @return counter for complexity
	 */
	ICounter getComplexityCounter();

	/**
	 * Returns the counter for methods.
	 *
	 * @return counter for methods
	 */
	ICounter getMethodCounter();

	/**
	 * Returns the counter for classes.
	 *
	 * @return counter for classes
	 */
	ICounter getClassCounter();

	/**
	 * Generic access to the the counters.
	 *
	 * @param entity
	 *            entity we're we want to have the counter for
	 * @return counter for the given entity
	 */
	ICounter getCounter(CounterEntity entity);

	/**
	 * Checks whether this node contains code relevant for code coverage.
	 *
	 * @return <code>true</code> if this node contains code relevant for code
	 *         coverage
	 */
	boolean containsCode();

	/**
	 * Creates a plain copy of this node. While {@link ICoverageNode}
	 * implementations may contain heavy data structures, the copy returned by
	 * this method is reduced to the counters only. This helps to save memory
	 * while processing huge structures.
	 *
	 * @return copy with counters only
	 */
	ICoverageNode getPlainCopy();

}
