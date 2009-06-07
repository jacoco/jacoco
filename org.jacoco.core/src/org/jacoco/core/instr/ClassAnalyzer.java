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
package org.jacoco.core.instr;

import org.jacoco.core.data.IClassStructureOutput;
import org.jacoco.core.data.IMethodStructureOutput;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.EmptyVisitor;

/**
 * A {@link ClassVisitor} that analyzes the executable blocks of a class.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ClassAnalyzer extends EmptyVisitor {

	private final IClassStructureOutput structureOutput;

	private int methodCount;

	/**
	 * Creates a new analyzer that reports to the given
	 * {@link IClassStructureOutput} instance.
	 * 
	 * @param structureOutput
	 *            consumer for class structure output
	 */
	public ClassAnalyzer(IClassStructureOutput structureOutput) {
		this.structureOutput = structureOutput;
		methodCount = 0;
	}

	@Override
	public void visitSource(String source, String debug) {
		if (source != null) {
			structureOutput.sourceFile(source);
		}
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {

		// Abstract methods do not have code to analyze
		if ((access & Opcodes.ACC_ABSTRACT) != 0) {
			return null;
		}

		final IMethodStructureOutput structure = structureOutput
				.methodStructure(methodCount++, name, desc, signature);
		return new BlockMethodAdapter(new MethodAnalyzer(structure), access,
				name, desc, signature, exceptions);
	}

	@Override
	public void visitEnd() {
		structureOutput.end();
	}

}
