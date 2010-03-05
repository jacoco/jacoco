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

import java.io.File;
import java.io.IOException;

import org.jacoco.core.data.IClassStructureVisitor;
import org.jacoco.core.data.IMethodStructureVisitor;
import org.jacoco.core.data.IStructureVisitor;
import org.junit.Test;

/**
 * Unit tests for {@link Analyzer}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class AnalyzerTest {

	@Test(expected = IOException.class)
	public void testInvalidDirectory() throws IOException {
		Analyzer analyzer = new Analyzer(new EmptyStructureVisitor());
		File invalid = new File("/this/path/should/not/exist/");
		analyzer.analyzeAll(invalid);
	}

	private static class EmptyStructureVisitor implements IStructureVisitor,
			IClassStructureVisitor, IMethodStructureVisitor {

		public IClassStructureVisitor visitClassStructure(long id) {
			return this;
		}

		public void visit(String name, String signature, String superName,
				String[] interfaces) {
		}

		public void visitSourceFile(String name) {
		}

		public IMethodStructureVisitor visitMethodStructure(String name,
				String desc, String signature) {
			return this;
		}

		public void block(int id, int instructionCount, int[] lineNumbers) {
		}

		public void visitEnd() {
		}

	}

}
