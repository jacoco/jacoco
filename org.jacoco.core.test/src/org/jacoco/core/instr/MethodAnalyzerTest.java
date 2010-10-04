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
package org.jacoco.core.instr;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jacoco.core.data.IMethodStructureVisitor;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link MethodAnalyzer}.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class MethodAnalyzerTest implements IMethodStructureVisitor {

	private IBlockMethodVisitor blockVisitor;

	private int id = -1;

	private int instructionCount = -1;

	private int[] lineNumbers = null;

	private boolean end = false;

	@Before
	public void setup() {
		blockVisitor = new MethodAnalyzer(this);
	}

	@Test
	public void testVisitBlockEndBeforeJump() {
		// Should not have any effect:
		blockVisitor.visitBlockEndBeforeJump(123);
		assertNoBlock();
	}

	@Test
	public void testVisitAnnotation() {
		blockVisitor.visitAnnotation("foo", false);
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 0);
	}

	@Test
	public void testVisitAnnotationDefault() {
		blockVisitor.visitAnnotationDefault();
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 0);
	}

	@Test
	public void testVisitParameterAnnotation() {
		blockVisitor.visitParameterAnnotation(0, "com/example/Foo", true);
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 0);
	}

	@Test
	public void testVisitAttribute() {
		blockVisitor.visitAttribute(new Attribute("com/example/Foo") {
		});
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 0);
	}

	@Test
	public void testVisitCode() {
		blockVisitor.visitCode();
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 0);
	}

	@Test
	public void testVisitFrame() {
		blockVisitor.visitFrame(Opcodes.F_NEW, 0, new Object[0], 0,
				new Object[0]);
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 0);
	}

	@Test
	public void testVisitLabel() {
		blockVisitor.visitLabel(new Label());
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 0);
	}

	@Test
	public void testLocalVariable() {
		blockVisitor.visitLocalVariable("i", "I", null, new Label(),
				new Label(), 0);
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 0);
	}

	@Test
	public void testVisitTryCatchBlock() {
		blockVisitor.visitTryCatchBlock(new Label(), new Label(), new Label(),
				"java/lang/Exception");
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 0);
	}

	@Test
	public void testVisitMaxs() {
		blockVisitor.visitMaxs(2, 2);
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 0);
	}

	@Test
	public void testVisitLineNumber1() {
		blockVisitor.visitLineNumber(10, new Label());
		blockVisitor.visitInsn(Opcodes.NOP);
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 1, 10);
	}

	@Test
	public void testVisitLineNumber2() {
		blockVisitor.visitLineNumber(10, new Label());
		blockVisitor.visitInsn(Opcodes.NOP);
		blockVisitor.visitLineNumber(15, new Label());
		blockVisitor.visitInsn(Opcodes.NOP);
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 2, 10, 15);
	}

	@Test
	public void testVisitLineNumber3() {
		blockVisitor.visitLineNumber(10, new Label());
		blockVisitor.visitInsn(Opcodes.NOP);
		blockVisitor.visitBlockEnd(123);
		this.lineNumbers = null;
		blockVisitor.visitInsn(Opcodes.NOP);
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 1, 10);
	}

	@Test
	public void testVisitInsn() {
		blockVisitor.visitInsn(Opcodes.RETURN);
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 1);
	}

	@Test
	public void testVisitJumpInsn() {
		blockVisitor.visitJumpInsn(Opcodes.GOTO, new Label());
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 1);
	}

	@Test
	public void testVisitFieldInsn() {
		blockVisitor.visitFieldInsn(Opcodes.GETFIELD, "Foo", "count", "I");
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 1);
	}

	@Test
	public void testVisitIincInsn() {
		blockVisitor.visitIincInsn(2, 1);
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 1);
	}

	@Test
	public void testVisitIntInsn() {
		blockVisitor.visitIntInsn(Opcodes.BIPUSH, 42);
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 1);
	}

	@Test
	public void testVisitLdcInsn() {
		blockVisitor.visitLdcInsn("Hello Stack!");
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 1);
	}

	@Test
	public void testVisitLookupSwitchInsn() {
		blockVisitor.visitLookupSwitchInsn(new Label(), new int[0],
				new Label[0]);
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 1);
	}

	@Test
	public void testVisitMethodInsn() {
		blockVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "com/example/Foo",
				"Test", "()V");
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 1);
	}

	@Test
	public void testVisitMultiANewArrayInsn() {
		blockVisitor.visitMultiANewArrayInsn("Z", 2);
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 1);
	}

	@Test
	public void testVisitTableSwitchInsn() {
		blockVisitor.visitTableSwitchInsn(0, 2, new Label(), new Label[0]);
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 1);
	}

	@Test
	public void testVisitTypeInsn() {
		blockVisitor.visitTypeInsn(Opcodes.INSTANCEOF, "com/example/Foo");
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 1);
	}

	@Test
	public void testVisitVarInsn() {
		blockVisitor.visitVarInsn(Opcodes.LLOAD, 2);
		blockVisitor.visitBlockEnd(123);
		assertBlock(123, 1);
	}

	@Test
	public void testEnd() {
		blockVisitor.visitEnd();
		assertTrue(end);
	}

	private void assertNoBlock() {
		assertEquals(-1, id, .0);
	}

	private void assertBlock(int id, int instructionCount, int... lineNumbers) {
		assertEquals(id, this.id, .0);
		assertEquals(instructionCount, this.instructionCount, .0);
		assertArrayEquals(lineNumbers, this.lineNumbers);
	}

	// === IMethodStructureVisitor ===

	public void block(int id, int instructionCount, int[] lineNumbers) {
		assertNull("Unexcpected call to block().", this.lineNumbers);
		this.id = id;
		this.instructionCount = instructionCount;
		this.lineNumbers = lineNumbers;
	}

	public void visitEnd() {
		this.end = true;
	}

}
