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
package org.jacoco.core.test.validation.java8;

import static org.jacoco.core.test.validation.targets.Stubs.exec;
import static org.jacoco.core.test.validation.targets.Stubs.noexec;
import static org.jacoco.core.test.validation.targets.Stubs.nop;

/**
 * This test target contains different lambda expressions.
 */
public class LambdaExpressionsTarget implements Runnable {

	@Override
	public void run() {
		
		exec(() -> {
			nop(); // $line-executedlambdabody$
		});
		
		noexec(() -> {
			nop(); // $line-notexecutedlambdabody$
		});
		
	}

	public static void main(String[] args) {
		new LambdaExpressionsTarget().run();
	}

}
