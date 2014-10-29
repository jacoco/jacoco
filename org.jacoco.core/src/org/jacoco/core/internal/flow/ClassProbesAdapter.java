/*******************************************************************************
 * Copyright (c) 2009, 2014 Mountainminds GmbH & Co. KG and Contributors
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

import org.jacoco.core.JaCoCo;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AnalyzerAdapter;

/**
 * A {@link org.objectweb.asm.ClassVisitor} that calculates probes for every
 * method.
 */
public class ClassProbesAdapter extends ClassVisitor implements
		IProbeIdGenerator {

	private final ClassProbesVisitor classProbesVisitor;

	private final boolean trackFrames;

	private int counter = 0;

	private String name;

	/**
	 * Creates a new adapter that delegates to the given visitor.
	 * 
	 * @param classProbesVisitor
	 *            instance to delegate to
	 * @param trackFrames
	 *            if <code>true</code> stackmap frames are tracked and provided
	 */
	public ClassProbesAdapter(final ClassProbesVisitor classProbesVisitor,
			final boolean trackFrames) {
		super(JaCoCo.ASM_API_VERSION, classProbesVisitor);
		this.classProbesVisitor = classProbesVisitor;
		this.trackFrames = trackFrames;
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		this.name = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public final MethodVisitor visitMethod(final int access, final String name,
			final String descriptor, final String signature,
			final String[] exceptions) {
		final MethodProbesVisitor methodProbesVisitor;
		final MethodProbesVisitor mv = classProbesVisitor.visitMethod(access, name, descriptor,
				signature, exceptions);
		if (mv == null) {
			// We need to visit the method in any case, otherwise probe ids
			// are not reproducible
			methodProbesVisitor = EmptyMethodProbesVisitor.getInstance();
		} else {
			methodProbesVisitor = mv;
		}
		return new MethodSanitizer(null, access, name, descriptor, signature,
				exceptions) {

			@Override
			public void visitEnd() {
				super.visitEnd();
				LabelFlowAnalyzer.markLabels(this);
				final MethodProbesAdapter probesAdapter = new MethodProbesAdapter(
						methodProbesVisitor, ClassProbesAdapter.this);
				if (trackFrames) {
					final AnalyzerAdapter analyzer = new AnalyzerAdapter(
							ClassProbesAdapter.this.name, access, name, desc,
							probesAdapter);
					probesAdapter.setAnalyzer(analyzer);
					this.accept(analyzer);
				} else {
					this.accept(probesAdapter);
				}
			}
		};
	}

	@Override
	public void visitEnd() {
		classProbesVisitor.visitTotalProbeCount(counter);
		super.visitEnd();
	}

	// === IProbeIdGenerator ===

	public int nextId() {
		return counter++;
	}

}
