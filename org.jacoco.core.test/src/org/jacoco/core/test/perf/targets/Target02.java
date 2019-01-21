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
package org.jacoco.core.test.perf.targets;

import java.util.concurrent.Callable;

/**
 * Simple Loop.
 */
public class Target02 implements Callable<Void> {

	public Void call() throws Exception {
		@SuppressWarnings("unused")
		int count = 0;
		for (int i = 0; i < 10000000; i++) {
			count++;
		}
		return null;
	}

}
