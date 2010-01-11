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
package org.jacoco.report;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link JavaNames}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class JavaNamesTest {

	private ILanguageNames names;

	@Before
	public void setup() {
		names = new JavaNames();
	}

	@Test
	public void testGetPackageName1() {
		assertEquals("default", names.getPackageName(""));
	}

	@Test
	public void testGetPackageName2() {
		assertEquals("java.lang", names.getPackageName("java/lang"));
	}

	@Test
	public void testGetClassName1() {
		assertEquals("Main", names.getClassName("Main"));
	}

	@Test
	public void testGetClassName2() {
		assertEquals("Object", names.getClassName("java/lang/Object"));
	}

	@Test
	public void testGetClassName3() {
		assertEquals("Map.Entry", names.getClassName("java/util/Map$Entry"));
	}

	@Test
	public void testGetMethodName1() {
		assertEquals("wait()", names.getMethodName("java/lang/Object", "wait",
				"()V"));
	}

	@Test
	public void testGetMethodName2() {
		assertEquals("remove(Object)", names.getMethodName(
				"java/util/Collection", "remove", "(Ljava/lang/Object;)V"));
	}

	@Test
	public void testGetMethodName3() {
		assertEquals("remove(int)", names.getMethodName("java/util/List",
				"remove", "(I)V"));
	}

	@Test
	public void testGetMethodName4() {
		assertEquals("add(int, Object)", names.getMethodName("java/util/List",
				"add", "(ILjava/lang/Object;)V"));
	}

	@Test
	public void testGetMethodName5() {
		assertEquals("sort(Object[])", names.getMethodName("java/util/Arrays",
				"sort", "([Ljava/lang/Object;)V"));
	}

	@Test
	public void testGetMethodName6() {
		assertEquals("Object()", names.getMethodName("java/lang/Object",
				"<init>", "()V"));
	}

	@Test
	public void testGetMethodName7() {
		assertEquals("static {...}", names.getMethodName(
				"com/example/SomeClass", "<clinit>", "()V"));
	}

	@Test
	public void testGetMethodName8() {
		assertEquals("update(Map.Entry)", names.getMethodName(
				"com/example/SomeClass", "update", "(Ljava/util/Map$Entry;)V"));
	}

}
