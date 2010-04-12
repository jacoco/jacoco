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
 * $Id: $
 *******************************************************************************/
package org.jacoco.ant;

import java.io.IOException;

import org.junit.Test;

/**
 * Simple test target for Java applications ant JUnit4 tests. To assert
 * execution it creates an empty file <code>target.txt</code> in the working
 * directory.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class TestTarget {

	@Test
	public void testNothing() throws IOException {
		System.out.println("Target executed");
	}

	public static void main(String[] args) throws IOException {
		System.out.println("Target executed");
	}

	/**
	 * @return location where this class is located
	 */
	public static String getClassPath() {
		final String name = TestTarget.class.getName();
		final String res = "/" + name.replace('.', '/') + ".class";
		final String loc = TestTarget.class.getResource(res).getFile();
		return loc.substring(0, loc.length() - res.length());
	}

}
