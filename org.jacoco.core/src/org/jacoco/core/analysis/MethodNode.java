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
 * Coverage data of a single method.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class MethodNode extends CoverageDataNodeImpl {

	private final String name;
	private final String desc;
	private final String signature;

	/**
	 * Creates a method coverage data object with the given parameters.
	 * 
	 * @param name
	 *            name of the method
	 * @param desc
	 *            parameter description
	 * @param signature
	 *            generic signature or <code>null</code>
	 * @param blocks
	 *            contained blocks
	 */
	public MethodNode(final String name, final String desc,
			final String signature, final Collection<ICoverageDataNode> blocks) {
		super(ElementType.METHOD, true);
		this.name = name;
		this.desc = desc;
		this.signature = signature;
		addAll(blocks);
		// A method is considered as covered when at least one block is covered:
		final boolean covered = getBlockCounter().getCoveredCount() > 0;
		methodCounter = CounterImpl.getInstance(covered);
	}

	/**
	 * Return the name of the method.
	 * 
	 * @return name of the method
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the parameter description of the method.
	 * 
	 * @return parameter description
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * Returns the generic signature of the method if defined.
	 * 
	 * @return generic signature or <code>null</code>
	 */
	public String getSignature() {
		return signature;
	}

}
