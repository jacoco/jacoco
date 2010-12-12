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
package org.jacoco.core.analysis;

import org.jacoco.core.data.IMethodStructureVisitor;

/**
 * Coverage data of a single method.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class MethodCoverage extends CoverageNodeImpl {

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
	 */
	public MethodCoverage(final String name, final String desc,
			final String signature) {
		super(ElementType.METHOD, name, true);
		this.desc = desc;
		this.signature = signature;
		this.methodCounter = CounterImpl.getInstance(false);
	}

	/**
	 * Adds a single instruction to this method.
	 * 
	 * @param covered
	 *            <code>true</code> if the instruction was executed
	 * @param line
	 *            source line number of the instruction
	 */
	public void addInsn(final boolean covered, final int line) {
		if (line != IMethodStructureVisitor.UNKNOWN_LINE) {
			this.lines.increment(line, covered);
		}
		this.instructionCounter = this.instructionCounter.increment(CounterImpl
				.getInstance(covered));
		if (covered && this.methodCounter.getCoveredCount() == 0) {
			this.methodCounter = CounterImpl.getInstance(true);
		}
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
