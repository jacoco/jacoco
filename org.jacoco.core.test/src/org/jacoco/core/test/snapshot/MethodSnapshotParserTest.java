/*******************************************************************************
 * Copyright (c) 2009, 2026 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.snapshot;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link MethodSnapshotParser}.
 */
public class MethodSnapshotParserTest {

	private final MethodNode m = new MethodNode();

	private final HashMap<String, AbstractInsnNode> comments = new HashMap<String, AbstractInsnNode>();

	@Test
	public void parseMaxStackMaxLocals() {
		m.visitInsn(Opcodes.NOP);
		m.visitMaxs(4, 2);
		final String text = text( //
				"    NOP", //
				"    MAXSTACK = 4", //
				"    MAXLOCALS = 2");
		assertEquals("text before parsing", text, toText(m));
		final MethodNode parsed = parse(text);
		assertEquals("text after parsing", text, toText(parsed));
		assertEquals(4, parsed.maxStack);
		assertEquals(2, parsed.maxLocals);
	}

	@Test
	public void parseLocalVariable() {
		final Label start = new Label();
		m.visitLocalVariable("object", "Ljava/lang/Object;", null, start,
				new Label(), 42);
		m.visitLocalVariable("list", "Ljava/util/List;",
				"Ljava/util/List<Ljava/lang/Integer;>;", start, new Label(),
				42);
		m.visitLabel(start);
		shouldParse(text( //
				"   L0", //
				"    LOCALVARIABLE object Ljava/lang/Object; L0 L1 42", //
				"    LOCALVARIABLE list Ljava/util/List; L0 L2 42", //
				"    // signature Ljava/util/List<Ljava/lang/Integer;>;", //
				"    // declaration: list extends java.util.List<java.lang.Integer>"));
	}

	@Test
	public void parseLineNumber() {
		m.visitLineNumber(5, new Label());
		shouldParse("    LINENUMBER 5 L0\n");
	}

	@Test
	public void parseTryCatchBlock() {
		final Label start = new Label();
		m.visitTryCatchBlock(start, new Label(), new Label(),
				"java/lang/Exception");
		m.visitLabel(start);
		shouldParse(text( //
				"    TRYCATCHBLOCK L0 L1 L2 java/lang/Exception", //
				"   L0"));
	}

	@Test
	public void parseTryCatchBlock_null_type() {
		final Label start = new Label();
		m.visitTryCatchBlock(start, new Label(), new Label(), null);
		m.visitLabel(start);
		final String text = text( //
				"    TRYCATCHBLOCK L0 L1 L2 null", //
				"   L0", //
				"    MAXSTACK = 0", //
				"    MAXLOCALS = 0");
		assertEquals("text before parsing", text, toText(m));
		final MethodNode parsed = parse(text);
		assertNull(parsed.tryCatchBlocks.get(0).type);
	}

	@Test
	public void parseFrame() {
		m.visitFrame(Opcodes.F_FULL, 1, new Object[] { Opcodes.LONG }, 1,
				new Object[] { Opcodes.LONG });
		m.visitFrame(Opcodes.F_APPEND, 1, new Object[] { Opcodes.LONG }, 0,
				null);
		m.visitFrame(Opcodes.F_CHOP, 2, null, 0, null);
		m.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		m.visitFrame(Opcodes.F_SAME1, 0, null, 1,
				new Object[] { Opcodes.LONG });
		final String text = text( //
				"   FRAME FULL [J] [J]", //
				"   FRAME APPEND [J]", //
				"   FRAME CHOP 2", //
				"   FRAME SAME", //
				"   FRAME SAME1 J", //
				"    MAXSTACK = 0", //
				"    MAXLOCALS = 0");
		assertEquals("text before parsing", text, toText(m));
		assertEquals("text after parsing", "", toText(parse(text)));
	}

	@Test
	public void parseLabel() {
		m.visitLabel(new Label());
		shouldParse("   L0\n");
	}

	@Test
	public void parseInsn() {
		m.visitInsn(Opcodes.ICONST_0);
		shouldParse("    ICONST_0\n");
	}

	@Test
	public void parseIntInsn() {
		m.visitIntInsn(Opcodes.BIPUSH, 42);
		shouldParse("    BIPUSH 42\n");
	}

	@Test
	public void parseLdcInsn() {
		m.visitLdcInsn("string");
		m.visitLdcInsn("string with \r\n new line");
		m.visitLdcInsn("string with \\ backslash");
		m.visitLdcInsn("string with \" double quote");
		m.visitLdcInsn("\uF000");
		m.visitLdcInsn(42D);
		m.visitLdcInsn(42F);
		m.visitLdcInsn(42L);
		m.visitLdcInsn(42);
		m.visitLdcInsn(Type.getType(String.class));
		m.visitLdcInsn(Type.getType(String[].class));
		m.visitLdcInsn(Type.getType(boolean[].class));
		m.visitLdcInsn(Type.getType(char[].class));
		m.visitLdcInsn(Type.getType(byte[].class));
		m.visitLdcInsn(Type.getType(short[].class));
		m.visitLdcInsn(Type.getType(int[].class));
		m.visitLdcInsn(Type.getType(float[].class));
		m.visitLdcInsn(Type.getType(long[].class));
		m.visitLdcInsn(Type.getType(double[].class));
		m.visitLdcInsn(Type.getMethodType(Type.getType(String.class),
				Type.getType(String.class)));
		m.visitLdcInsn(new Handle(Opcodes.H_INVOKESTATIC, "Owner", "Name",
				"(Descriptor)V", true));
		m.visitLdcInsn(new Handle(Opcodes.H_GETFIELD, "Owner", "Name",
				"Descriptor", false));
		shouldParse(text( //
				"    LDC \"string\"", // CONSTANT_String
				"    LDC \"string with \\r\\n new line\"", //
				"    LDC \"string with \\\\ backslash\"", //
				"    LDC \"string with \\\" double quote\"", //
				"    LDC \"\\uf000\"", //
				"    LDC 42.0D", // CONSTANT_Double
				"    LDC 42.0F", // CONSTANT_Float
				"    LDC 42L", // CONSTANT_Long
				"    LDC 42", // CONSTANT_Integer
				"    LDC Ljava/lang/String;.class", // CONSTANT_Class
				"    LDC [Ljava/lang/String;.class", //
				"    LDC [Z.class", //
				"    LDC [C.class", //
				"    LDC [B.class", //
				"    LDC [S.class", //
				"    LDC [I.class", //
				"    LDC [F.class", //
				"    LDC [J.class", //
				"    LDC [D.class", //
				"    LDC (Ljava/lang/String;)Ljava/lang/String;.class", // CONSTANT_MethodType
				"    // handle kind 0x6 : INVOKESTATIC", // CONSTANT_MethodHandle
				"    LDC Owner.Name(Descriptor)V itf", //
				"    // handle kind 0x1 : GETFIELD", // CONSTANT_MethodHandle
				"    LDC Owner.Name(Descriptor)" //
		));
	}

	@Test
	public void parseLdcInsn_ConstantDynamic() {
		m.visitLdcInsn(new ConstantDynamic("Name", "Descriptor",
				new Handle(Opcodes.H_INVOKESTATIC, "HandleOwner", "HandleName",
						"(HandleDescriptor)V", false)));
		shouldParse(text( //
				"    LDC Name : Descriptor [", // CONSTANT_Dynamic
				"      // handle kind 0x6 : INVOKESTATIC", //
				"      HandleOwner.HandleName(HandleDescriptor)V", //
				"      // arguments: none", //
				"    ]"));
	}

	@Test
	public void parseLdcInsn_ConstantDynamic_arguments() {
		m.visitLdcInsn(new ConstantDynamic("Name", "Descriptor",
				new Handle(Opcodes.H_INVOKESTATIC, "HandleOwner", "HandleName",
						"(HandleDescriptor)V", false),
				Type.getType(String.class), new Handle(Opcodes.H_INVOKEVIRTUAL,
						"Owner", "Name", "(Descriptor)V", true)));
		shouldParse(text( //
				"    LDC Name : Descriptor [", // CONSTANT_Dynamic
				"      // handle kind 0x6 : INVOKESTATIC", //
				"      HandleOwner.HandleName(HandleDescriptor)V", //
				"      // arguments:", //
				"      java.lang.String.class, ", //
				"      // handle kind 0x5 : INVOKEVIRTUAL", // CONSTANT_MethodHandle
				"      Owner.Name(Descriptor)V itf", //
				"    ]"));
	}

	@Test
	public void parseVarInsn() {
		m.visitVarInsn(Opcodes.ILOAD, 1);
		m.visitVarInsn(Opcodes.LLOAD, 2);
		m.visitVarInsn(Opcodes.FLOAD, 3);
		m.visitVarInsn(Opcodes.DLOAD, 4);
		m.visitVarInsn(Opcodes.ALOAD, 5);
		m.visitVarInsn(Opcodes.ISTORE, 6);
		m.visitVarInsn(Opcodes.LSTORE, 7);
		m.visitVarInsn(Opcodes.FSTORE, 8);
		m.visitVarInsn(Opcodes.DSTORE, 9);
		m.visitVarInsn(Opcodes.ASTORE, 10);
		shouldParse(text( //
				"    ILOAD 1", //
				"    LLOAD 2", //
				"    FLOAD 3", //
				"    DLOAD 4", //
				"    ALOAD 5", //
				"    ISTORE 6", //
				"    LSTORE 7", //
				"    FSTORE 8", //
				"    DSTORE 9", //
				"    ASTORE 10"));
	}

	@Test
	public void parseIincInsn() {
		m.visitIincInsn(4, 2);
		shouldParse(text("    IINC 4 2"));
	}

	@Test
	public void parseJumpInsn() {
		m.visitJumpInsn(Opcodes.IFEQ, new Label());
		m.visitJumpInsn(Opcodes.IFNE, new Label());
		m.visitJumpInsn(Opcodes.IFNE, new Label());
		m.visitJumpInsn(Opcodes.IFLT, new Label());
		m.visitJumpInsn(Opcodes.IFGE, new Label());
		m.visitJumpInsn(Opcodes.IFGT, new Label());
		m.visitJumpInsn(Opcodes.IFLE, new Label());
		m.visitJumpInsn(Opcodes.IF_ICMPEQ, new Label());
		m.visitJumpInsn(Opcodes.IF_ICMPNE, new Label());
		m.visitJumpInsn(Opcodes.IF_ICMPLT, new Label());
		m.visitJumpInsn(Opcodes.IF_ICMPGE, new Label());
		m.visitJumpInsn(Opcodes.IF_ICMPGT, new Label());
		m.visitJumpInsn(Opcodes.IF_ICMPLE, new Label());
		m.visitJumpInsn(Opcodes.IF_ACMPEQ, new Label());
		m.visitJumpInsn(Opcodes.IF_ACMPNE, new Label());
		m.visitJumpInsn(Opcodes.GOTO, new Label());
		m.visitJumpInsn(Opcodes.IFNULL, new Label());
		m.visitJumpInsn(Opcodes.IFNONNULL, new Label());
		shouldParse(text( //
				"    IFEQ L0", //
				"    IFNE L1", //
				"    IFNE L2", //
				"    IFLT L3", //
				"    IFGE L4", //
				"    IFGT L5", //
				"    IFLE L6", //
				"    IF_ICMPEQ L7", //
				"    IF_ICMPNE L8", //
				"    IF_ICMPLT L9", //
				"    IF_ICMPGE L10", //
				"    IF_ICMPGT L11", //
				"    IF_ICMPLE L12", //
				"    IF_ACMPEQ L13", //
				"    IF_ACMPNE L14", //
				"    GOTO L15", //
				"    IFNULL L16", //
				"    IFNONNULL L17"));
	}

	@Test
	public void parseTableSwitchInsn() {
		m.visitTableSwitchInsn(2, 3, new Label(), new Label(), new Label());
		shouldParse(text( //
				"    TABLESWITCH", //
				"      2: L0", //
				"      3: L1", //
				"      default: L2" //
		));
	}

	@Test
	public void parseLookupSwitchInsn() {
		m.visitLookupSwitchInsn(new Label(), new int[] { 1, 3 },
				new Label[] { new Label(), new Label() });
		shouldParse(text( //
				"    LOOKUPSWITCH", //
				"      1: L0", //
				"      3: L1", //
				"      default: L2"));
	}

	@Test
	public void parseFieldInsn() {
		m.visitFieldInsn(Opcodes.GETSTATIC, "owner", "name", "descriptor");
		m.visitFieldInsn(Opcodes.PUTSTATIC, "owner", "name", "descriptor");
		m.visitFieldInsn(Opcodes.GETFIELD, "owner", "name", "descriptor");
		m.visitFieldInsn(Opcodes.PUTFIELD, "owner", "name", "descriptor");
		shouldParse(text( //
				"    GETSTATIC owner.name : descriptor", //
				"    PUTSTATIC owner.name : descriptor", //
				"    GETFIELD owner.name : descriptor", //
				"    PUTFIELD owner.name : descriptor"));
	}

	@Test
	public void parseMethodInsn() {
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "owner", "name",
				"(descriptor)V", true);
		m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "owner", "name",
				"(descriptor)V", false);
		m.visitMethodInsn(Opcodes.INVOKESPECIAL, "owner", "name",
				"(descriptor)V", false);
		m.visitMethodInsn(Opcodes.INVOKESTATIC, "owner", "name",
				"(descriptor)V", false);
		m.visitMethodInsn(Opcodes.INVOKEINTERFACE, "owner", "name",
				"(descriptor)V", false);
		shouldParse(text( //
				"    INVOKEVIRTUAL owner.name (descriptor)V (itf)", //
				"    INVOKEVIRTUAL owner.name (descriptor)V", //
				"    INVOKESPECIAL owner.name (descriptor)V", //
				"    INVOKESTATIC owner.name (descriptor)V", //
				"    INVOKEINTERFACE owner.name (descriptor)V"));
	}

	@Test
	public void parseInvokeDynamicInsn() {
		m.visitInvokeDynamicInsn("name", "(descriptor)I",
				new Handle(Opcodes.H_INVOKESTATIC, "HandleOwner", "HandleName",
						"(HandleDescriptor)V", false));
		shouldParse(text( //
				"    INVOKEDYNAMIC name(descriptor)I [", //
				"      // handle kind 0x6 : INVOKESTATIC", //
				"      HandleOwner.HandleName(HandleDescriptor)V", //
				"      // arguments: none", //
				"    ]"));
	}

	@Test
	public void parseInvokeDynamicInsn_arguments() {
		m.visitInvokeDynamicInsn("name", "(descriptor)I",
				new Handle(Opcodes.H_INVOKESTATIC, "HandleOwner", "HandleName",
						"(HandleDescriptor)V", false),
				Type.getMethodType("()V"), //
				Type.getType(String.class), //
				Type.getType(String[].class), //
				Type.getType(boolean[].class), //
				Type.getType(char[].class), //
				Type.getType(byte[].class), //
				Type.getType(short[].class), //
				Type.getType(int[].class), //
				Type.getType(float[].class), //
				Type.getType(long[].class), //
				Type.getType(double[].class));
		shouldParse(text( //
				"    INVOKEDYNAMIC name(descriptor)I [", //
				"      // handle kind 0x6 : INVOKESTATIC", // CONSTANT_MethodHandle
				"      HandleOwner.HandleName(HandleDescriptor)V", //
				"      // arguments:", //
				"      ()V, ", // CONSTANT_MethodType
				"      java.lang.String.class, ", // CONSTANT_Class
				"      java.lang.String[].class, ", //
				"      boolean[].class, ", //
				"      char[].class, ", //
				"      byte[].class, ", //
				"      short[].class, ", //
				"      int[].class, ", //
				"      float[].class, ", //
				"      long[].class, ", //
				"      double[].class", //
				"    ]"));
	}

	/** For example in case of toString in records (Java 16). */
	@Test
	public void handleKindGetField() {
		m.visitInvokeDynamicInsn("toString", "(Target)Ljava/lang/String;",
				new Handle(Opcodes.H_INVOKESTATIC,
						"java/lang/runtime/ObjectMethods", "bootstrap",
						"()Ljava/lang/Object;", false),
				Type.getObjectType("Target"), //
				"field", //
				new Handle(Opcodes.H_GETFIELD, "Target", "field", "I", false));
		shouldParse(text( //
				"    INVOKEDYNAMIC toString(Target)Ljava/lang/String; [", //
				"      // handle kind 0x6 : INVOKESTATIC", //
				"      java/lang/runtime/ObjectMethods.bootstrap()Ljava/lang/Object;", //
				"      // arguments:", //
				"      Target.class, ", //
				"      \"field\", ", //
				"      // handle kind 0x1 : GETFIELD", //
				"      Target.field(I)", //
				"    ]"));
	}

	/** For example in case of method reference (Java 8). */
	@Test
	public void handleKindInvokeVirtual() {
		m.visitInvokeDynamicInsn("name", "(descriptor)I",
				new Handle(Opcodes.H_INVOKESTATIC, "LambdaMetafactory",
						"metafactory", "()LCallSite;", false),
				new Handle(Opcodes.H_INVOKEVIRTUAL, "Example", "target", "()V",
						false));
		shouldParse(text( //
				"    INVOKEDYNAMIC name(descriptor)I [", //
				"      // handle kind 0x6 : INVOKESTATIC", //
				"      LambdaMetafactory.metafactory()LCallSite;",
				"      // arguments:", //
				"      // handle kind 0x5 : INVOKEVIRTUAL", //
				"      Example.target()V", //
				"    ]")); //
	}

	/** For example in case of constructor method reference (Java 8). */
	@Test
	public void handleKindNewInvokeSpecial() {
		m.visitInvokeDynamicInsn("name", "(descriptor)I",
				new Handle(Opcodes.H_INVOKESTATIC, "LambdaMetafactory",
						"metafactory", "()LCallSite;", false),
				new Handle(Opcodes.H_NEWINVOKESPECIAL, "Target", "init", "()V",
						false));
		shouldParse(text( //
				"    INVOKEDYNAMIC name(descriptor)I [", //
				"      // handle kind 0x6 : INVOKESTATIC", //
				"      LambdaMetafactory.metafactory()LCallSite;",
				"      // arguments:", //
				"      // handle kind 0x8 : NEWINVOKESPECIAL", //
				"      Target.init()V", //
				"    ]")); //
	}

	@Test
	public void parseTypeInsn() {
		m.visitTypeInsn(Opcodes.NEW, "java/lang/Object");
		m.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
		m.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
		m.visitTypeInsn(Opcodes.INSTANCEOF, "java/lang/String");
		shouldParse(text( //
				"    NEW java/lang/Object", //
				"    ANEWARRAY java/lang/Object", //
				"    CHECKCAST java/lang/String", //
				"    INSTANCEOF java/lang/String"));
	}

	@Test
	public void parseIntInsn_NEWARRAY() {
		m.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
		m.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_LONG);
		shouldParse(text( //
				"    NEWARRAY T_BOOLEAN", //
				"    NEWARRAY T_LONG"));
	}

	@Test
	public void parseMultiANewArrayInsn() {
		m.visitMultiANewArrayInsn("descriptor", 42);
		shouldParse("    MULTIANEWARRAY descriptor 42\n");
	}

	@Test
	public void parseInsn_JSR() {
		m.visitInsn(Opcodes.JSR);
		final String text = toText(m);
		try {
			parse(text);
			fail("expected UnsupportedOperationException");
		} catch (final UnsupportedOperationException e) {
			// expected
		}
	}

	@Test
	public void parseInsn_RET() {
		m.visitInsn(Opcodes.RET);
		final String text = toText(m);
		try {
			parse(text);
			fail("expected UnsupportedOperationException");
		} catch (final UnsupportedOperationException e) {
			// expected
		}
	}

	@Test
	public void parseComments() {
		final MethodNode parsed = parse(text( //
				"// c1", //
				"NOP", //
				"// c2", //
				"NOP", //
				"// c3"));
		final HashMap<String, AbstractInsnNode> expected = new HashMap<String, AbstractInsnNode>();
		expected.put("// c1", null);
		expected.put("// c2", parsed.instructions.getFirst());
		expected.put("// c3", parsed.instructions.getLast());
		assertEquals(expected, comments);
	}

	/**
	 * Asserts that parsing of given {@code text} produces equivalent of
	 * {@link #m}.
	 */
	private void shouldParse(String text) {
		text += text( //
				"    MAXSTACK = 0", //
				"    MAXLOCALS = 0");
		assertEquals("text before parsing", text, toText(m));
		final MethodNode parsed = parse(text);
		assertEquals("text after parsing", text, toText(parsed));
		assertArrayEquals("method bytes", toBytes(m), toBytes(parsed));
		assertTrue(comments.isEmpty());
	}

	private MethodNode parse(final String text) {
		try {
			return MethodSnapshot.parse(new StringReader(text),
					new MethodSnapshotCommentsHandler() {
						public void onComment(final String comment,
								final AbstractInsnNode precedingInstruction) {
							assertNull(comments.put(comment,
									precedingInstruction));
						}
					});
		} catch (final IOException e) {
			// Must not happen with StringReader
			throw new IllegalStateException(e);
		}
	}

	private static String toText(final MethodNode m) {
		return MethodSnapshot.snapshot(m);
	}

	private static byte[] toBytes(final MethodNode m) {
		final ClassWriter classWriter = new ClassWriter(0);
		classWriter.visit(Opcodes.V11, 0, "Example", null, "java/lang/Object",
				null);
		final MethodVisitor methodWriter = classWriter.visitMethod(0, "method",
				"()V", null, null);
		m.accept(new MethodSnapshotVisitor(methodWriter));
		return classWriter.toByteArray();
	}

	/**
	 * Poor man's replacement for <a href="https://openjdk.org/jeps/378">Java 15
	 * Text Blocks</a>.
	 */
	static String text(final String... text) {
		final StringBuilder stringBuilder = new StringBuilder();
		for (final String line : text) {
			stringBuilder.append(line).append('\n');
		}
		return stringBuilder.toString();
	}

}
