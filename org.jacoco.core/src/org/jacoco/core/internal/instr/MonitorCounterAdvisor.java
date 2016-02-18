/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import org.jacoco.core.data.ProbeMode;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * An advice that can be added to a method visitor that adds counting of the
 * number of active monitor style locks active on any thread. This class works
 * in conjunction with the {@code ThreadLocalMonitorCounter} class that
 * maintains the accumulators. The {@code ProbeMode.parallelcount} uses this
 * counter to determine if when a probe is increments, it is in serial or
 * parallel mode (i.e. no locks held)
 * 
 * @author Omer Azmon
 */
public class MonitorCounterAdvisor extends AdviceAdapter {
	static final String COUNTER_CLASS_NAME = ThreadLocalMonitorCounter.class
			.getName().replace('.', '/');
	private final boolean shouldMonitor;
	private final boolean shouldWrap;

	/**
	 * Creates a new {@link AdviceAdapter} for counting the number of active
	 * monitors.
	 * 
	 * @param api
	 *            the ASM API version implemented by this visitor.
	 * @param mv
	 *            the method visitor to which this adapter delegates calls.
	 * @param access
	 *            the method's access flags (see {@link Opcodes}).
	 * @param name
	 *            the method's name.
	 * @param desc
	 *            the method's descriptor (see {@link Type Type}).
	 */
	public MonitorCounterAdvisor(final int api, final MethodVisitor mv,
			final int access, final String name, final String desc) {
		super(api, mv, access, name, desc);
		shouldMonitor = ProbeArrayService.getProbeMode() == ProbeMode.parallelcount;
		shouldWrap = shouldMonitor && isSynchronized(access);
	}

	/**
	 * Should the advisor advise tracking monitors
	 * 
	 * @return {@code true} if this the advise is to track monitors; Otherwise,
	 *         {@code false}.
	 */
	public boolean isShouldMonitor() {
		return shouldMonitor;
	}

	/**
	 * Should the advisor advise wrapping the method provided in construction
	 * 
	 * @return {@code true} if this the advise is to wrap the method; Otherwise,
	 *         {@code false}.
	 */
	public boolean isShouldWrap() {
		return shouldWrap;
	}

	@Override
	protected void onMethodEnter() {
		if (shouldWrap) {
			injectIncrement();
		}
	}

	@Override
	protected void onMethodExit(final int opcode) {
		if (shouldWrap) {
			injectDecrement();
		}
	}

	@Override
	public void visitInsn(final int opcode) {
		if (shouldMonitor) {
			switch (opcode) {
			case Opcodes.MONITORENTER:
				injectIncrement();
				super.visitInsn(opcode);
				break;
			case Opcodes.MONITOREXIT:
				super.visitInsn(opcode);
				injectDecrement();
				break;
			default:
				super.visitInsn(opcode);
				break;
			}
		} else {
			super.visitInsn(opcode);
		}
	}

	private static final boolean isSynchronized(final int access) {
		return (access & Opcodes.ACC_SYNCHRONIZED) != 0;
	}

	private void injectIncrement() {
		super.visitMethodInsn(Opcodes.INVOKESTATIC, COUNTER_CLASS_NAME,
				"increment", "()V", false);
	}

	private void injectDecrement() {
		super.visitMethodInsn(Opcodes.INVOKESTATIC, COUNTER_CLASS_NAME,
				"decrement", "()V", false);
	}

}
