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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;

/**
 * Parser for {@link MethodSnapshot#snapshot(MethodNode) snapshots} of a
 * {@link MethodNode} bytecode.
 */
final class MethodSnapshotParser {

	private static final HashMap<String, Integer> OPCODES = new HashMap<String, Integer>();
	private static final HashMap<String, Integer> TYPES = new HashMap<String, Integer>();
	static {
		for (int opcode = 0; opcode < Textifier.OPCODES.length; opcode++) {
			OPCODES.put(Textifier.OPCODES[opcode], opcode);
		}
		for (int type = 0; type < Textifier.TYPES.length; type++) {
			TYPES.put(Textifier.TYPES[type], type);
		}
	}

	private final Lexer input;
	private final MethodNode visitor;
	private final HashMap<String, Label> labels = new HashMap<String, Label>();

	private final MethodSnapshotCommentsHandler commentsHandler;

	MethodSnapshotParser(final Reader reader, final MethodNode visitor,
			final MethodSnapshotCommentsHandler commentsHandler) {
		this.input = new Lexer(reader);
		this.visitor = visitor;
		this.commentsHandler = commentsHandler;
	}

	void parse() throws IOException {
		while (input.peekToken() != null) {
			final String opcodeName = input.nextToken();
			final Integer opcode = OPCODES.get(opcodeName);
			if (opcode == null) {
				parseNonOpcode(opcodeName);
			} else {
				switch (opcode) {
				case Opcodes.BIPUSH:
				case Opcodes.SIPUSH:
					parseIntInsn(opcode);
					break;
				case Opcodes.LDC:
					parseLdcInsn(null);
					break;
				case Opcodes.ILOAD:
				case Opcodes.LLOAD:
				case Opcodes.FLOAD:
				case Opcodes.DLOAD:
				case Opcodes.ALOAD:
				case Opcodes.ISTORE:
				case Opcodes.LSTORE:
				case Opcodes.FSTORE:
				case Opcodes.DSTORE:
				case Opcodes.ASTORE:
					parseVar(opcode);
					break;
				case Opcodes.IINC:
					parseIinc();
					break;
				case Opcodes.IFEQ:
				case Opcodes.IFNE:
				case Opcodes.IFLT:
				case Opcodes.IFGE:
				case Opcodes.IFGT:
				case Opcodes.IFLE:
				case Opcodes.IF_ICMPEQ:
				case Opcodes.IF_ICMPNE:
				case Opcodes.IF_ICMPLT:
				case Opcodes.IF_ICMPGE:
				case Opcodes.IF_ICMPGT:
				case Opcodes.IF_ICMPLE:
				case Opcodes.IF_ACMPEQ:
				case Opcodes.IF_ACMPNE:
				case Opcodes.GOTO:
				case Opcodes.IFNULL:
				case Opcodes.IFNONNULL:
					parseJump(opcode);
					break;
				case Opcodes.JSR:
				case Opcodes.RET:
					throw new UnsupportedOperationException();
				case Opcodes.TABLESWITCH:
					parseTableSwitch();
					break;
				case Opcodes.LOOKUPSWITCH:
					parseLookupSwitch();
					break;
				case Opcodes.GETSTATIC:
				case Opcodes.PUTSTATIC:
				case Opcodes.GETFIELD:
				case Opcodes.PUTFIELD:
					parseField(opcode);
					break;
				case Opcodes.INVOKEVIRTUAL:
				case Opcodes.INVOKESPECIAL:
				case Opcodes.INVOKESTATIC:
				case Opcodes.INVOKEINTERFACE:
					parseMethod(opcode);
					break;
				case Opcodes.INVOKEDYNAMIC:
					parseInvokeDynamic();
					break;
				case Opcodes.NEW:
				case Opcodes.ANEWARRAY:
				case Opcodes.CHECKCAST:
				case Opcodes.INSTANCEOF:
					parseType(opcode);
					break;
				case Opcodes.NEWARRAY:
					parseNewArray();
					break;
				case Opcodes.MULTIANEWARRAY:
					parseMultiANewArray();
					break;
				default:
					visitor.visitInsn(opcode);
					break;
				}
			}
		}
	}

	private void parseNonOpcode(final String name) throws IOException {
		if ("MAXSTACK".equals(name)) {
			parseMaxStackMaxLocals();

		} else if ("LOCALVARIABLE".equals(name)) {
			parseLocalVariable();

		} else if ("LINENUMBER".equals(name)) {
			parseLineNumber();

		} else if (name.startsWith("L")) {
			parseLabel(name);

		} else if ("TRYCATCHBLOCK".equals(name)) {
			parseTryCatchBlock();

		} else if ("FRAME".equals(name)) {
			parseFrame();

		} else if (name.startsWith("// handle kind")) {
			if (!input.nextToken().equals("LDC")) {
				throw new IllegalStateException();
			}
			parseLdcInsn(name);

		} else if (name.startsWith("//")) {
			if (commentsHandler != null) {
				commentsHandler.onComment(name, visitor.instructions.getLast());
			}

		} else {
			throw new IllegalStateException(name);
		}
	}

	private void parseMaxStackMaxLocals() throws IOException {
		if (!"=".equals(input.nextToken())) {
			throw new IllegalStateException();
		}
		final int maxStack = Integer.parseInt(input.nextToken());
		if (!"MAXLOCALS".equals(input.nextToken())) {
			throw new IllegalStateException();
		}
		if (!"=".equals(input.nextToken())) {
			throw new IllegalStateException();
		}
		final int maxLocals = Integer.parseInt(input.nextToken());
		visitor.visitMaxs(maxStack, maxLocals);
	}

	private void parseLocalVariable() throws IOException {
		final String name = input.nextToken();
		final String descriptor = input.nextToken();
		final Label start = getLabel(input.nextToken());
		final Label end = getLabel(input.nextToken());
		final int index = Integer.parseInt(input.nextToken());
		final String peekToken = input.peekToken();
		final String signature;
		if (peekToken != null && peekToken.startsWith("// signature ")) {
			signature = input.nextToken().substring("// signature ".length());
			input.nextToken(); // declaration
		} else {
			signature = null;
		}
		visitor.visitLocalVariable(name, descriptor, signature, start, end,
				index);
	}

	private void parseLineNumber() throws IOException {
		final int line = Integer.parseInt(input.nextToken());
		final Label start = getLabel(input.nextToken());
		visitor.visitLineNumber(line, start);
	}

	private void parseLabel(final String name) {
		final Label label = getLabel(name);
		visitor.visitLabel(label);
	}

	private void parseTryCatchBlock() throws IOException {
		final Label start = getLabel(input.nextToken());
		final Label end = getLabel(input.nextToken());
		final Label handler = getLabel(input.nextToken());
		final String type = input.nextToken();
		visitor.visitTryCatchBlock(start, end, handler,
				"null".equals(type) ? null : type);
	}

	private void parseFrame() throws IOException {
		final String frame = input.nextToken();
		String token;
		if ("FULL".equals(frame)) {
			do {
				token = input.nextToken(); // local
			} while (!token.endsWith("]"));
			do {
				token = input.nextToken(); // stack
			} while (!token.endsWith("]"));

		} else if ("APPEND".equals(frame)) {
			do {
				token = input.nextToken(); // local
			} while (!token.endsWith("]"));

		} else if ("CHOP".equals(frame)) {
			input.nextToken(); // numLocals

		} else if ("SAME1".equals(frame)) {
			input.nextToken(); // type

		} else if (!"SAME".equals(frame)) {
			throw new IllegalStateException(frame);
		}
	}

	private void parseIntInsn(final int opcode) throws IOException {
		final int operand = Integer.parseInt(input.nextToken());
		visitor.visitIntInsn(opcode, operand);
	}

	private void parseLdcInsn(final String handleKindComment)
			throws IOException {
		final Object value = parseConstant(true, handleKindComment);
		visitor.visitLdcInsn(value);
	}

	private Object parseConstant() throws IOException {
		final String peekToken = input.peekToken();
		String handleKindComment;
		if (peekToken != null && peekToken.startsWith("// handle kind")) {
			handleKindComment = input.nextToken();
		} else {
			handleKindComment = null;
		}
		return parseConstant(false, handleKindComment);
	}

	/**
	 * Note that {@link Textifier} uses {@link Type#getDescriptor()} for
	 * {@link org.objectweb.asm.tree.LdcInsnNode#cst}, whereas
	 * {@link Type#getClassName()} for
	 * {@link org.objectweb.asm.tree.InvokeDynamicInsnNode#bsmArgs} and
	 * {@link ConstantDynamic#getBootstrapMethodArgument(int)}.
	 */
	private Object parseConstant(final boolean ldcOperand,
			final String handleKindComment) throws IOException {
		final String operand = input.nextToken();
		Object value;
		if (":".equals(input.peekToken())) { // CONSTANT_Dynamic
			value = parseConstantDynamic(operand);

		} else if (operand.startsWith("\"")) { // CONSTANT_String
			if (!operand.endsWith("\"")) {
				throw new IllegalStateException();
			}
			value = operand.substring(1, operand.length() - 1);

		} else if (operand.endsWith("D")) { // CONSTANT_Double
			value = Double.parseDouble(operand);

		} else if (operand.endsWith("F")) { // CONSTANT_Float
			value = Float.parseFloat(operand);

		} else if (operand.endsWith("L")) { // CONSTANT_Long
			value = Long.parseLong(operand.substring(0, operand.length() - 1));

		} else if (operand.endsWith(".class")) { // CONSTANT_Class
			final String s = operand.substring(0,
					operand.length() - ".class".length());
			final String descriptor = ldcOperand ? s : classNameToDescriptor(s);
			value = Type.getType(descriptor);

		} else if (operand.startsWith("(")) { // CONSTANT_MethodType
			value = Type.getType(operand);

		} else if (operand.contains(".")) { // CONSTANT_MethodHandle
			value = parseHandle(handleKindComment, operand);

		} else { // CONSTANT_Integer
			value = Integer.parseInt(operand);

		}
		return value;
	}

	private ConstantDynamic parseConstantDynamic(final String name)
			throws IOException {
		input.nextToken(); // :
		final String descriptor = input.nextToken();
		if (!"[".equals(input.nextToken())) {
			throw new IllegalStateException();
		}
		final Handle handle = parseHandle(input.nextToken(), input.nextToken());
		if (!input.nextToken().startsWith("// arguments:")) {
			throw new IllegalStateException();
		}
		final ArrayList<Object> arguments = new ArrayList<Object>();
		while (!"]".equals(input.peekToken())) {
			arguments.add(parseConstant());
		}
		input.nextToken(); // ]
		return new ConstantDynamic(name, descriptor, handle,
				arguments.toArray());
	}

	private String classNameToDescriptor(String className) {
		final StringBuilder descriptor = new StringBuilder();
		int end = className.length();
		while (className.charAt(end - 1) == ']') {
			end -= 2; // []
			descriptor.append("[");
		}
		className = className.substring(0, end);
		if ("void".equals(className)) {
			descriptor.append(Type.getDescriptor(void.class));
		} else if ("boolean".equals(className)) {
			descriptor.append(Type.getDescriptor(boolean.class));
		} else if ("char".equals(className)) {
			descriptor.append(Type.getDescriptor(char.class));
		} else if ("byte".equals(className)) {
			descriptor.append(Type.getDescriptor(byte.class));
		} else if ("short".equals(className)) {
			descriptor.append(Type.getDescriptor(short.class));
		} else if ("int".equals(className)) {
			descriptor.append(Type.getDescriptor(int.class));
		} else if ("float".equals(className)) {
			descriptor.append(Type.getDescriptor(float.class));
		} else if ("long".equals(className)) {
			descriptor.append(Type.getDescriptor(long.class));
		} else if ("double".equals(className)) {
			descriptor.append(Type.getDescriptor(double.class));
		} else {
			descriptor.append("L").append(className.replace('.', '/'))
					.append(";");
		}
		return descriptor.toString();
	}

	private void parseVar(final int opcode) throws IOException {
		final int varIndex = Integer.parseInt(input.nextToken());
		visitor.visitVarInsn(opcode, varIndex);
	}

	private void parseIinc() throws IOException {
		final int varIndex = Integer.parseInt(input.nextToken());
		final int increment = Integer.parseInt(input.nextToken());
		visitor.visitIincInsn(varIndex, increment);
	}

	private void parseJump(final int opcode) throws IOException {
		final String labelName = input.nextToken();
		visitor.visitJumpInsn(opcode, getLabel(labelName));
	}

	private Label getLabel(final String labelName) {
		Label label = labels.get(labelName);
		if (label == null) {
			label = new Label();
			labels.put(labelName, label);
		}
		return label;
	}

	private void parseTableSwitch() throws IOException {
		final ArrayList<Label> labels = new ArrayList<Label>();
		final Label dflt;
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		do {
			final String key = input.nextToken();
			final String labelName = input.nextToken();
			if ("default:".equals(key)) {
				dflt = getLabel(labelName);
				break;
			}
			if (!key.endsWith(":")) {
				throw new IllegalStateException(key);
			}
			final int value = Integer
					.parseInt(key.substring(0, key.length() - 1));
			min = Math.min(min, value);
			max = Math.max(max, value);
			labels.add(getLabel(labelName));
		} while (true);
		visitor.visitTableSwitchInsn(min, max, dflt,
				labels.toArray(new Label[0]));
	}

	private void parseLookupSwitch() throws IOException {
		Label dflt;
		final ArrayList<Integer> keys = new ArrayList<Integer>();
		final ArrayList<Label> labels = new ArrayList<Label>();
		do {
			final String key = input.nextToken();
			final String labelName = input.nextToken();
			if ("default:".equals(key)) {
				dflt = getLabel(labelName);
				break;
			}
			if (!key.endsWith(":")) {
				throw new IllegalStateException(key);
			}
			final int value = Integer
					.parseInt(key.substring(0, key.length() - 1));
			keys.add(value);
			labels.add(getLabel(labelName));
		} while (true);
		int[] intKeys = new int[keys.size()];
		for (int i = 0; i < keys.size(); i++) {
			intKeys[i] = keys.get(i);
		}
		visitor.visitLookupSwitchInsn(dflt, intKeys,
				labels.toArray(new Label[0]));
	}

	private void parseField(final int opcode) throws IOException {
		final String[] dotSeparatedOwnerAndName = input.nextToken().split("\\.",
				2);
		final String owner = dotSeparatedOwnerAndName[0];
		final String name = dotSeparatedOwnerAndName[1];
		if (!":".equals(input.nextToken())) {
			throw new IllegalStateException();
		}
		final String descriptor = input.nextToken();
		visitor.visitFieldInsn(opcode, owner, name, descriptor);
	}

	private void parseMethod(final int opcode) throws IOException {
		final String[] dotSeparatedOwnerAndName = input.nextToken().split("\\.",
				2);
		final String owner = dotSeparatedOwnerAndName[0];
		final String name = dotSeparatedOwnerAndName[1];
		final String descriptor = input.nextToken();
		final boolean itf = "(itf)".equals(input.peekToken());
		if (itf) {
			input.nextToken();
		}
		visitor.visitMethodInsn(opcode, owner, name, descriptor, itf);
	}

	private void parseInvokeDynamic() throws IOException {
		final String nameDescriptor = input.nextToken();
		final int nameEnd = nameDescriptor.indexOf('(');
		final String name = nameDescriptor.substring(0, nameEnd);
		final String descriptor = nameDescriptor.substring(nameEnd);
		if (!"[".equals(input.nextToken())) {
			throw new IllegalStateException();
		}
		final Handle bootstrapMethodHandle = parseHandle(input.nextToken(),
				input.nextToken());
		if (!input.nextToken().startsWith("// arguments:")) {
			throw new IllegalStateException();
		}
		final ArrayList<Object> arguments = new ArrayList<Object>();
		while (!"]".equals(input.peekToken())) {
			arguments.add(parseConstant());
		}
		input.nextToken(); // ]
		visitor.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle,
				arguments.toArray());
	}

	private Handle parseHandle(final String handleKindComment,
			final String handle) throws IOException {
		final int tag = getHandleKind(handleKindComment);
		final int ownerEnd = handle.indexOf('.');
		final String owner = handle.substring(0, ownerEnd);
		final int nameEnd = handle.indexOf('(', ownerEnd);
		final String name = handle.substring(ownerEnd + 1, nameEnd);
		final String descriptor = handle.substring(nameEnd);
		final boolean itf = "itf".equals(input.peekToken());
		if (itf) {
			input.nextToken();
		}
		if (descriptor.endsWith(")")) {
			final String fieldDescriptor = descriptor.substring(1,
					descriptor.length() - 1);
			return new Handle(tag, owner, name, fieldDescriptor, false);
		}
		return new Handle(tag, owner, name, descriptor, itf);
	}

	private int getHandleKind(final String handleKindComment) {
		if ("// handle kind 0x1 : GETFIELD".equals(handleKindComment)) {
			return Opcodes.H_GETFIELD;
		} else if ("// handle kind 0x5 : INVOKEVIRTUAL"
				.equals(handleKindComment)) {
			return Opcodes.H_INVOKEVIRTUAL;
		} else if ("// handle kind 0x6 : INVOKESTATIC"
				.equals(handleKindComment)) {
			return Opcodes.H_INVOKESTATIC;
		} else if ("// handle kind 0x8 : NEWINVOKESPECIAL"
				.equals(handleKindComment)) {
			return Opcodes.H_NEWINVOKESPECIAL;
		} else {
			throw new IllegalStateException(handleKindComment);
		}
	}

	private void parseType(final int opcode) throws IOException {
		final String type = input.nextToken();
		visitor.visitTypeInsn(opcode, type);
	}

	private void parseNewArray() throws IOException {
		final String operand = input.nextToken();
		visitor.visitIntInsn(Opcodes.NEWARRAY, TYPES.get(operand));
	}

	private void parseMultiANewArray() throws IOException {
		final String descriptor = input.nextToken();
		final int numDimensions = Integer.parseInt(input.nextToken());
		visitor.visitMultiANewArrayInsn(descriptor, numDimensions);
	}

	/**
	 * Lexer with {@link #peekToken() one-token lookahead}.
	 */
	static class Lexer {

		private final Reader reader;

		Lexer(final Reader reader) {
			this.reader = reader;
		}

		/** Last read character. */
		private int c;

		private String token;

		public String nextToken() throws IOException {
			final String token = peekToken();
			this.token = null;
			return token;
		}

		public String peekToken() throws IOException {
			if (token != null) {
				return token;
			}
			do {
				c = reader.read();
			} while (Character.isWhitespace(c));
			if (c == -1) {
				// end of input
				return null;
			} else if (c == '/') {
				token = lexComment();
			} else if (c == '"') {
				token = lexString();
			} else {
				token = lexWord();
			}
			return token;
		}

		private String lexWord() throws IOException {
			final StringBuilder s = new StringBuilder();
			do {
				s.append((char) c);
				c = reader.read();
			} while (c != -1 && c != ',' && !Character.isWhitespace(c));
			return s.toString();
		}

		private String lexString() throws IOException {
			final StringBuilder s = new StringBuilder();
			while (true) {
				s.append((char) c);
				c = reader.read();
				switch (c) {
				case '"':
					c = reader.read();
					if (!Character.isWhitespace(c) && c != ',' && c != -1) {
						throw new IllegalStateException(
								"Improperly terminated string token");
					}
					s.append('"');
					return s.toString();
				case '\\':
					c = reader.read();
					if (c == 'n') {
						c = '\n';
					} else if (c == 'r') {
						c = '\r';
					} else if (c == 'u') {
						c = Integer.parseInt("" //
								+ (char) reader.read() //
								+ (char) reader.read() //
								+ (char) reader.read() //
								+ (char) reader.read(), //
								16);
					} else if (c != '"' && c != '\\') {
						throw new IllegalStateException(
								"Invalid escape in string token");
					}
					break;
				case -1:
					throw new IllegalStateException(
							"Unterminated string token");
				}
			}
		}

		private String lexComment() throws IOException {
			final StringBuilder s = new StringBuilder();
			do {
				s.append((char) c);
				c = reader.read();
			} while (c != -1 && c != '\n');
			return s.toString();
		}
	}
}
