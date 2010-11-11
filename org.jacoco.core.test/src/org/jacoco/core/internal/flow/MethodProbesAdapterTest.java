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
package org.jacoco.core.internal.flow;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link MethodProbesAdapter}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class MethodProbesAdapterTest implements IProbeIdGenerator,
		InvocationHandler {

	private Label label;

	private int id;

	private List<String> methodName;

	private List<Object[]> methodArgs;

	private MethodVisitor adapter;

	@Before
	public void setup() {
		label = new Label();
		id = 1000;
		methodName = new ArrayList<String>();
		methodArgs = new ArrayList<Object[]>();
		IMethodProbesVisitor probesVistor = (IMethodProbesVisitor) Proxy
				.newProxyInstance(getClass().getClassLoader(),
						new Class[] { IMethodProbesVisitor.class }, this);
		adapter = new MethodProbesAdapter(probesVistor, this);
	}

	@Test
	public void testVisitProbe1() {
		LabelInfo.setTarget(label);
		LabelInfo.setSuccessor(label);

		adapter.visitLabel(label);

		assertEquals(2, methodName.size());
		assertEquals("visitProbe", methodName.get(0));
		assertEquals(Integer.valueOf(1000), methodArgs.get(0)[0]);
		assertEquals("visitLabel", methodName.get(1));
		assertEquals(label, methodArgs.get(1)[0]);
	}

	@Test
	public void testVisitProbe2() {
		adapter.visitLabel(label);

		assertEquals(1, methodName.size());
		assertEquals("visitLabel", methodName.get(0));
		assertEquals(label, methodArgs.get(0)[0]);
	}

	@Test
	public void testVisitProbe3() {
		adapter.visitLabel(label);

		assertEquals(1, methodName.size());
		assertEquals("visitLabel", methodName.get(0));
		assertEquals(label, methodArgs.get(0)[0]);
	}

	@Test
	public void testVisitInsn1() {
		adapter.visitInsn(Opcodes.RETURN);

		assertEquals(1, methodName.size());
		assertEquals("visitInsnWithProbe", methodName.get(0));
		assertEquals(Integer.valueOf(Opcodes.RETURN), methodArgs.get(0)[0]);
	}

	@Test
	public void testVisitInsn2() {
		adapter.visitInsn(Opcodes.IADD);

		assertEquals(1, methodName.size());
		assertEquals("visitInsn", methodName.get(0));
		assertEquals(Integer.valueOf(Opcodes.IADD), methodArgs.get(0)[0]);
	}

	@Test
	public void testVisitJumpInsn1() {
		LabelInfo.setTarget(label);
		LabelInfo.setTarget(label);

		adapter.visitJumpInsn(Opcodes.IFLT, label);

		assertEquals(1, methodName.size());
		assertEquals("visitJumpInsnWithProbe", methodName.get(0));
		assertEquals(Integer.valueOf(Opcodes.IFLT), methodArgs.get(0)[0]);
		assertEquals(label, methodArgs.get(0)[1]);
		assertEquals(Integer.valueOf(1000), methodArgs.get(0)[2]);
	}

	@Test
	public void testVisitJumpInsn2() {
		adapter.visitJumpInsn(Opcodes.IFLT, label);

		assertEquals(1, methodName.size());
		assertEquals("visitJumpInsn", methodName.get(0));
		assertEquals(Integer.valueOf(Opcodes.IFLT), methodArgs.get(0)[0]);
		assertEquals(label, methodArgs.get(0)[1]);
	}

	// === IProbeIdGenerator ===

	public int nextId() {
		return id++;
	}

	// === InvocationHandler ===

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		methodName.add(method.getName());
		methodArgs.add(args);
		return null;
	}

}
