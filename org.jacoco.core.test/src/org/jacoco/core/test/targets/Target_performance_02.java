package org.jacoco.core.test.targets;

/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Marc R. Hoffmann - initial API and implementation
 * 
 * $Id: $
 *******************************************************************************/

/**
 * Loop within a method.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class Target_performance_02 implements Runnable {

	public void run() {
		int count = 0;
		for (int i = 0; i < 2000000; i++) {
			count++;
		}
	}

	@Override
	public String toString() {
		return "loop";
	}

}
