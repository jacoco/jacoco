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
package org.jacoco.core.instr;

import org.jacoco.core.data.IClassStructureVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * Analyzes the structure of a class.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
class ClassAnalyzer extends EmptyVisitor implements IBlockClassVisitor {

	private final IClassStructureVisitor structureVisitor;

	/**
	 * Creates a new analyzer that reports to the given
	 * {@link IClassStructureVisitor} instance.
	 * 
	 * @param structureVisitor
	 *            consumer for class structure output
	 */
	public ClassAnalyzer(final IClassStructureVisitor structureVisitor) {
		this.structureVisitor = structureVisitor;
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		structureVisitor.visit(name, signature, superName, interfaces);
	}

	@Override
	public void visitSource(final String source, final String debug) {
		if (source != null) {
			structureVisitor.visitSourceFile(source);
		}
	}

	@Override
	public IBlockMethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {

		// TODO: Use filter hook
		if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
			return null;
		}

		return new MethodAnalyzer(structureVisitor.visitMethodStructure(name,
				desc, signature));
	}

	@Override
	public void visitEnd() {
		structureVisitor.visitEnd();
	}

	public void visitTotalProbeCount(final int count) {
	}

}
