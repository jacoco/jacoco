/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak -initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.report.xml;

import java.io.IOException;

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.MethodCoverage;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.report.IReportVisitor;

/**
 * Wrapper for an {@link XMLElement} that contains method coverage data
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class MethodNode extends NodeWithCoverage {

	private static final CounterEntity[] METHOD_COUNTERS = {
			CounterEntity.BLOCK, CounterEntity.LINE, CounterEntity.INSTRUCTION };

	/**
	 * Creates a new Method coverage element for the supplied package and class
	 * coverage node
	 * 
	 * @param parent
	 *            Parent element that will own this class element
	 * @param methodNode
	 *            Method coverage node
	 * @throws IOException
	 *             IO Error creating the element
	 */
	public MethodNode(final ClassNode parent, final MethodCoverage methodNode)
			throws IOException {
		super(parent, "method", methodNode);
		this.attr("desc", methodNode.getDesc());
		final String signature = methodNode.getSignature();
		if (signature != null) {
			this.attr("signature", signature);
		}
	}

	public IReportVisitor visitChild(final ICoverageNode node)
			throws IOException {
		throw new IllegalStateException("Methods must not have child nodes.");
	}

	@Override
	protected CounterEntity[] getCounterEntities() {
		return METHOD_COUNTERS;
	}

}
