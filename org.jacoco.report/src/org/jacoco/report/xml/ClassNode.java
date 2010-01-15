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

import org.jacoco.core.analysis.ClassCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.report.IReportVisitor;

/**
 * Wrapper for an {@link XMLElement} that contains class coverage data
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class ClassNode extends NodeWithCoverage {
	private static final CounterEntity[] CLASS_COUNTERS = {
			CounterEntity.METHOD, CounterEntity.BLOCK, CounterEntity.LINE,
			CounterEntity.INSTRUCTION, };

	/**
	 * Creates a new Class coverage element for the supplied package and class
	 * coverage node
	 * 
	 * @param parent
	 *            Parent element that will own this class element
	 * @param classNode
	 *            Class coverage node
	 * @throws IOException
	 *             IO Error creating the element
	 */
	public ClassNode(final PackageNode parent, final ICoverageNode classNode)
			throws IOException {
		super(parent, "class", classNode);
		final ClassCoverage classCoverageNode = (ClassCoverage) classNode;
		if (classCoverageNode.getSignature() != null) {
			this.attr("signature", classCoverageNode.getSignature());
		}
		if (classCoverageNode.getSuperName() != null) {
			this.attr("superclass", classCoverageNode.getSuperName());
		}
		if (classCoverageNode.getInterfaceNames() != null) {
			boolean first = true;
			final StringBuilder builder = new StringBuilder();
			for (final String iface : classCoverageNode.getInterfaceNames()) {
				if (first) {
					first = false;
				} else {
					builder.append(' ');
				}
				builder.append(iface);
			}
			this.attr("interfaces", builder.toString());
		}
	}

	public IReportVisitor visitChild(final ICoverageNode node)
			throws IOException {

		return new MethodNode(this, node);
	}

	@Override
	protected CounterEntity[] getCounterEntities() {
		return CLASS_COUNTERS;
	}

}
