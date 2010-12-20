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

import static org.junit.Assert.assertArrayEquals;
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
		assertMethodCall(0, "visitProbe", Integer.valueOf(1000));
		assertMethodCall(1, "visitLabel", label);
	}

	@Test
	public void testVisitProbe2() {
		adapter.visitLabel(label);

		assertEquals(1, methodName.size());
		assertMethodCall(0, "visitLabel", label);
	}

	@Test
	public void testVisitInsn1() {
		adapter.visitInsn(Opcodes.RETURN);

		assertEquals(1, methodName.size());
		assertMethodCall(0, "visitInsnWithProbe",
				Integer.valueOf(Opcodes.RETURN), Integer.valueOf(1000));
	}

	@Test
	public void testVisitInsn2() {
		adapter.visitInsn(Opcodes.IADD);

		assertEquals(1, methodName.size());
		assertMethodCall(0, "visitInsn", Integer.valueOf(Opcodes.IADD));
	}

	@Test
	public void testVisitJumpInsn1() {
		LabelInfo.setTarget(label);
		LabelInfo.setTarget(label);

		adapter.visitJumpInsn(Opcodes.IFLT, label);

		assertEquals(1, methodName.size());
		assertMethodCall(0, "visitJumpInsnWithProbe",
				Integer.valueOf(Opcodes.IFLT), label, Integer.valueOf(1000));
	}

	@Test
	public void testVisitJumpInsn2() {
		adapter.visitJumpInsn(Opcodes.IFLT, label);

		assertEquals(1, methodName.size());
		assertMethodCall(0, "visitJumpInsn", Integer.valueOf(Opcodes.IFLT),
				label);
	}

	@Test
	public void testVisitLookupSwitchInsn1() {
		LabelInfo.setTarget(label);
		LabelInfo.setTarget(label);

		final int[] keys = new int[] { 0, 1 };
		final Label[] labels = new Label[] { label, label };
		adapter.visitLookupSwitchInsn(label, keys, labels);

		assertEquals(1, methodName.size());
		assertMethodCall(0, "visitLookupSwitchInsnWithProbes", label, keys,
				labels);
		assertEquals(1000, LabelInfo.getProbeId(label));
	}

	@Test
	public void testVisitLookupSwitchInsn2() {
		Label label2 = new Label();
		LabelInfo.setTarget(label2);
		LabelInfo.setTarget(label2);

		final int[] keys = new int[] { 0, 1 };
		final Label[] labels = new Label[] { label2, label };
		adapter.visitLookupSwitchInsn(label, keys, labels);

		assertEquals(1, methodName.size());
		assertMethodCall(0, "visitLookupSwitchInsnWithProbes", label, keys,
				labels);
		assertEquals(LabelInfo.NO_PROBE, LabelInfo.getProbeId(label));
		assertEquals(1000, LabelInfo.getProbeId(label2));
	}

	@Test
	public void testVisitLookupSwitchInsn3() {
		final int[] keys = new int[] { 0, 1 };
		final Label[] labels = new Label[] { label, label };
		adapter.visitLookupSwitchInsn(label, keys, labels);

		assertEquals(1, methodName.size());
		assertMethodCall(0, "visitLookupSwitchInsn", label, keys, labels);
	}

	@Test
	public void testVisitTableSwitchInsn1() {
		LabelInfo.setTarget(label);
		LabelInfo.setTarget(label);

		final Label[] labels = new Label[] { label, label };
		adapter.visitTableSwitchInsn(0, 1, label, labels);

		assertEquals(1, methodName.size());
		assertMethodCall(0, "visitTableSwitchInsnWithProbes",
				Integer.valueOf(0), Integer.valueOf(1), label, labels);
		assertEquals(1000, LabelInfo.getProbeId(label));
	}

	@Test
	public void testVisitTableSwitchInsn2() {
		Label label2 = new Label();
		LabelInfo.setTarget(label2);
		LabelInfo.setTarget(label2);

		final Label[] labels = new Label[] { label2, label };
		adapter.visitTableSwitchInsn(0, 1, label, labels);

		assertEquals(1, methodName.size());
		assertMethodCall(0, "visitTableSwitchInsnWithProbes",
				Integer.valueOf(0), Integer.valueOf(1), label, labels);
		assertEquals(LabelInfo.NO_PROBE, LabelInfo.getProbeId(label));
		assertEquals(1000, LabelInfo.getProbeId(label2));
	}

	@Test
	public void testVisitTableSwitchInsn3() {
		final Label[] labels = new Label[] { label, label };
		adapter.visitTableSwitchInsn(0, 1, label, labels);

		assertEquals(1, methodName.size());
		assertMethodCall(0, "visitTableSwitchInsn", Integer.valueOf(0),
				Integer.valueOf(1), label, labels);
	}

	private void assertMethodCall(int idx, String name, Object... params) {
		assertEquals(name, methodName.get(idx));
		assertArrayEquals(params, methodArgs.get(idx));
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
