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

	public static class MethodDataRecorder implements IMethodStructureVisitor {

		private final int id;

		private final String name, desc, signature;

		private final List<BlockData> blocks = new ArrayList<BlockData>();

		MethodDataRecorder(int id, String name, String desc, String signature) {
			this.id = id;
			this.name = name;
			this.desc = desc;
			this.signature = signature;
		}

		public void block(int id, int instructionCount, int[] lineNumbers) {
			assertTrue("Unexpected block id " + id, blocks.size() == id);
			blocks.add(new BlockData(instructionCount, lineNumbers));
		}

		public void visitEnd() {
		}

		public BlockData getBlock(int id) {
			return blocks.get(id);
		}
	}

	public static class BlockData {

		private final int instructionCount;

		private final int[] lineNumbers;

		private boolean covered;

		BlockData(int instructionCount, int[] lineNumbers) {
			this.instructionCount = instructionCount;
			this.lineNumbers = lineNumbers;
		}

		void setCovered(boolean covered) {
			this.covered = covered;
		}

		public void assertCovered() {
			assertTrue(covered);
		}

		public void assertNotCovered() {
			assertFalse(covered);
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

	public MethodDataRecorder getMethod(String name) {
		MethodDataRecorder m = methodsByName.get(name);
		assertNotNull("Unkown method " + name, m);
		return m;
	}

	// === ICoverageDataVisitor ===

	public void visitClassExecution(long id, String name, boolean[][] blockdata) {
		assertTrue("Coverage and structure data for the same class only.",
				classid == id);
		assertTrue("Unexpected method count " + blockdata.length, methods
				.size() == blockdata.length);
		for (int i = 0; i < blockdata.length; i++) {
			final MethodDataRecorder m = methods.get(i);
			for (int j = 0; j < blockdata[i].length; j++) {
				m.getBlock(j).setCovered(blockdata[i][j]);
			}
		}
	}

	// === IStructureVisitor ===

	public IClassStructureVisitor visitClassStructure(long id, String name) {
		assertTrue("Recorder can be used for a single class only.", id != -1);
		classid = id;
		return this;
	}

	// === IClassStructureVisitor ===

	public void visitSourceFile(String name) {
	}

	public IMethodStructureVisitor visitMethodStructure(int id, String name,
			String desc, String signature) {
		MethodDataRecorder m = new MethodDataRecorder(id, name, desc, signature);
		methods.add(m);
		methodsByName.put(name, m);
		return m;
	}

	public void visitEnd() {
	}

}
