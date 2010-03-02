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
package org.jacoco.core.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jacoco.core.data.IClassStructureVisitor;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.IMethodStructureVisitor;
import org.jacoco.core.data.IStructureVisitor;

/**
 * Recorder for structure as well as coverage data for a single class providing
 * different assertion mechanisms for testing.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
class ClassDataRecorder implements IExecutionDataVisitor, IStructureVisitor,
		IClassStructureVisitor {

	public class MethodDataRecorder implements IMethodStructureVisitor {

		private final String name, desc, signature;

		private final List<BlockData> blocks = new ArrayList<BlockData>();

		MethodDataRecorder(String name, String desc, String signature) {
			this.name = name;
			this.desc = desc;
			this.signature = signature;
		}

		public void block(int id, int instructionCount, int[] lineNumbers) {
			blocks.add(new BlockData(id, instructionCount, lineNumbers));
		}

		public void visitEnd() {
		}

		public BlockData getBlock(int id) {
			return blocks.get(id);
		}
	}

	public class BlockData {

		private final int id;

		private final int instructionCount;

		private final int[] lineNumbers;

		BlockData(int id, int instructionCount, int[] lineNumbers) {
			this.id = id;
			this.instructionCount = instructionCount;
			this.lineNumbers = lineNumbers;
		}

		public boolean isCovered() {
			return data[id];
		}

		public void assertCovered() {
			assertTrue(isCovered());
		}

		public void assertMissed() {
			assertFalse(isCovered());
		}

		public void assertLines(int... expected) {
			assertTrue("Unexpected line count " + lineNumbers.length,
					expected.length == lineNumbers.length);
			for (int i = 0; i < expected.length; i++) {
				final Integer e = Integer.valueOf(expected[i]);
				final Integer a = Integer.valueOf(lineNumbers[i]);
				assertTrue("Unexpected line " + a, e == a);
			}
		}

	}

	private long classid = -1L;

	private final List<MethodDataRecorder> methods = new ArrayList<MethodDataRecorder>();

	private final Map<String, MethodDataRecorder> methodsByName = new HashMap<String, MethodDataRecorder>();

	private boolean[] data;

	public MethodDataRecorder getMethod(String name) {
		MethodDataRecorder m = methodsByName.get(name);
		assertNotNull("Unkown method " + name, m);
		return m;
	}

	// === IExecutionDataVisitor ===

	public void visitClassExecution(long id, String name, boolean[] data) {
		assertTrue("Coverage and structure data for the same class only.",
				classid == id);
		this.data = data;
	}

	// === IStructureVisitor ===

	public IClassStructureVisitor visitClassStructure(long id) {
		assertTrue("Recorder can be used for a single class only.", id != -1);
		classid = id;
		return this;
	}

	// === IClassStructureVisitor ===

	public void visit(String name, String signature, String superName,
			String[] interfaces) {
	}

	public void visitSourceFile(String name) {
	}

	public IMethodStructureVisitor visitMethodStructure(String name,
			String desc, String signature) {
		MethodDataRecorder m = new MethodDataRecorder(name, desc, signature);
		methods.add(m);
		methodsByName.put(name, m);
		return m;
	}

	public void visitEnd() {
	}

}
