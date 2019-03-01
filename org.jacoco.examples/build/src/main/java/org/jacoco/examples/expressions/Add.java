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
package org.jacoco.examples.expressions;

public class Add implements IExpression {

	private final IExpression l;

	private final IExpression r;

	public Add(final IExpression l, final IExpression r) {
		this.l = l;
		this.r = r;
	}

	public double evaluate() {
		return l.evaluate() + r.evaluate();
	}

}
