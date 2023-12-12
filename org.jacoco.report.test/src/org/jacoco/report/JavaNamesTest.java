/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link JavaNames}.
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
		assertEquals("Main", names.getClassName("Main", null, null, null));
	}

	@Test
	public void testGetClassName2() {
		assertEquals("Object",
				names.getClassName("java/lang/Object", null, null, null));
	}

	@Test
	public void testGetClassName3() {
		assertEquals("Map.Entry",
				names.getClassName("java/util/Map$Entry", null, null, null));
	}

	@Test
	public void testGetClassName4() {
		assertEquals("Bar.new Object() {...}", names.getClassName(
				"com/foo/Bar$1", null, "java/lang/Object", new String[0]));
	}

	@Test
	public void testGetClassName5() {
		assertEquals("Bar.new ISample() {...}",
				names.getClassName("com/foo/Bar$1", null, "java/lang/Object",
						new String[] { "org/foo/ISample" }));
	}

	@Test
	public void testGetClassName6() {
		assertEquals("Bar.1",
				names.getClassName("com/foo/Bar$1", null, null, null));
	}

	@Test
	public void testGetClassName7() {
		assertEquals("Strange.",
				names.getClassName("com/foo/Strange$", null, null, null));
	}

	@Test
	public void testGetQualifiedClassName1() {
		assertEquals("Foo", names.getQualifiedClassName("Foo"));
	}

	@Test
	public void testGetQualifiedClassName2() {
		assertEquals("java.lang.Object",
				names.getQualifiedClassName("java/lang/Object"));
	}

	@Test
	public void testGetQualifiedClassName3() {
		assertEquals("java.util.Map.Entry",
				names.getQualifiedClassName("java/util/Map$Entry"));
	}

	@Test
	public void testGetMethodName1() {
		assertEquals("wait()",
				names.getMethodName("java/lang/Object", "wait", "()V", null));
	}

	@Test
	public void testGetMethodName2() {
		assertEquals("remove(Object)",
				names.getMethodName("java/util/Collection", "remove",
						"(Ljava/lang/Object;)V", null));
	}

	@Test
	public void testGetMethodName3() {
		assertEquals("remove(int)",
				names.getMethodName("java/util/List", "remove", "(I)V", null));
	}

	@Test
	public void testGetMethodName4() {
		assertEquals("add(int, Object)", names.getMethodName("java/util/List",
				"add", "(ILjava/lang/Object;)V", null));
	}

	@Test
	public void testGetMethodName5() {
		assertEquals("sort(Object[])", names.getMethodName("java/util/Arrays",
				"sort", "([Ljava/lang/Object;)V", null));
	}

	@Test
	public void testGetMethodName6() {
		assertEquals("Object()",
				names.getMethodName("java/lang/Object", "<init>", "()V", null));
	}

	@Test
	public void testGetMethodName7() {
		assertEquals("static {...}", names.getMethodName(
				"com/example/SomeClass", "<clinit>", "()V", null));
	}

	@Test
	public void testGetMethodName8() {
		assertEquals("update(Map.Entry)",
				names.getMethodName("com/example/SomeClass", "update",
						"(Ljava/util/Map$Entry;)V", null));
	}

	@Test
	public void testGetMethodName9() {
		assertEquals("{...}", names.getMethodName("com/example/SomeClass$1",
				"<init>", "()V", null));
	}

	@Test
	public void testGetQualifiedMethodName() {
		assertEquals("java.util.List.add(int, java.lang.Object)",
				names.getQualifiedMethodName("java/util/List", "add",
						"(ILjava/lang/Object;)V", null));
	}

}
