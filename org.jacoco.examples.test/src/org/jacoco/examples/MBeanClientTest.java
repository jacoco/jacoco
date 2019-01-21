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
package org.jacoco.examples;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jacoco.agent.rt.IAgent;
import org.junit.Test;

/**
 * Tests for {@link MBeanClient}.
 */
public class MBeanClientTest {

	@Test
	public void testMBeanInterfaceCompatibility() {
		Set<String> expected = getDeclaredMethods(IAgent.class);
		Set<String> actual = getDeclaredMethods(MBeanClient.IProxy.class);
		assertEquals(expected, actual);
	}

	private Set<String> getDeclaredMethods(Class<?> clazz) {
		Set<String> methods = new HashSet<String>();
		for (Method m : clazz.getDeclaredMethods()) {
			methods.add(String.format("%s %s(%s)", m.getReturnType().getName(),
					m.getName(), Arrays.asList(m.getParameterTypes())));
		}
		return methods;
	}

}
