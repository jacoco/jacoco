/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.internal.instr;

import org.jacoco.core.JaCoCo;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * This method adapter tracks the state of the local variable and stack types.
 * With insertFrame() additional frames can then be added. The adapter is only
 * intended to be used with class file versions >= {@link Opcodes#V1_6}.
 */
class FrameTracker extends MethodVisitor implements IFrameInserter {

	private final String owner;

	private Object[] local;
	private int localSize;
	private Object[] stack;
	private int stackSize;

	public FrameTracker(final String owner, final int access,
			final String name, final String desc, final MethodVisitor mv) {
		super(JaCoCo.ASM_API_VERSION, mv);
		this.owner = owner;
		local = new Object[8];
		localSize = 0;
		stack = new Object[8];
		stackSize = 0;

		if ((access & Opcodes.ACC_STATIC) == 0) {
			if ("<init>".equals(name)) {
				set(localSize, Opcodes.UNINITIALIZED_THIS);
			} else {
				set(localSize, owner);
			}
		}
		for (final Type t : Type.getArgumentTypes(desc)) {
			set(localSize, t);
		}

	}

	public void insertFrame() {
		// Reduced types do not need more space than expanded types:
		final Object[] local = new Object[this.localSize];
		final Object[] stack = new Object[this.stackSize];
		final int localSize = reduce(this.local, this.localSize, local);
		final int stackSize = reduce(this.stack, this.stackSize, stack);
		mv.visitFrame(Opcodes.F_NEW, localSize, local, stackSize, stack);
	}

	@Override
	public void visitFrame(final int type, final int nLocal,
			final Object[] local, final int nStack, final Object[] stack) {

		if (type != Opcodes.F_NEW) {
			throw new IllegalArgumentException(
					"ClassReader.accept() should be called with EXPAND_FRAMES flag");
		}

		// expanded types need at most twice the size
		this.local = ensureSize(this.local, nLocal * 2);
		this.stack = ensureSize(this.stack, nStack * 2);
		this.localSize = expand(local, nLocal, this.local);
		this.stackSize = expand(stack, nStack, this.stack);

		mv.visitFrame(type, nLocal, local, nStack, stack);
	}

	@Override
	public void visitInsn(final int opcode) {
		final Object t1, t2, t3, t4;
		switch (opcode) {
		case Opcodes.NOP:
		case Opcodes.RETURN:
			break;
		case Opcodes.ARETURN:
		case Opcodes.ATHROW:
		case Opcodes.FRETURN:
		case Opcodes.IRETURN:
		case Opcodes.MONITORENTER:
		case Opcodes.MONITOREXIT:
		case Opcodes.POP:
			pop(1);
			break;
		case Opcodes.DRETURN:
		case Opcodes.LRETURN:
		case Opcodes.POP2:
			pop(2);
			break;
		case Opcodes.AASTORE:
		case Opcodes.BASTORE:
		case Opcodes.CASTORE:
		case Opcodes.FASTORE:
		case Opcodes.IASTORE:
		case Opcodes.SASTORE:
			pop(3);
			break;
		case Opcodes.LASTORE:
		case Opcodes.DASTORE:
			pop(4);
			break;
		case Opcodes.ICONST_M1:
		case Opcodes.ICONST_0:
		case Opcodes.ICONST_1:
		case Opcodes.ICONST_2:
		case Opcodes.ICONST_3:
		case Opcodes.ICONST_4:
		case Opcodes.ICONST_5:
			push(Opcodes.INTEGER);
			break;
		case Opcodes.ARRAYLENGTH:
		case Opcodes.F2I:
		case Opcodes.I2B:
		case Opcodes.I2C:
		case Opcodes.I2S:
		case Opcodes.INEG:
			pop(1);
			push(Opcodes.INTEGER);
			break;
		case Opcodes.BALOAD:
		case Opcodes.CALOAD:
		case Opcodes.D2I:
		case Opcodes.FCMPG:
		case Opcodes.FCMPL:
		case Opcodes.IADD:
		case Opcodes.IALOAD:
		case Opcodes.IAND:
		case Opcodes.IDIV:
		case Opcodes.IMUL:
		case Opcodes.IOR:
		case Opcodes.IREM:
		case Opcodes.ISHL:
		case Opcodes.ISHR:
		case Opcodes.ISUB:
		case Opcodes.IUSHR:
		case Opcodes.IXOR:
		case Opcodes.L2I:
		case Opcodes.SALOAD:
			pop(2);
			push(Opcodes.INTEGER);
			break;
		case Opcodes.DCMPG:
		case Opcodes.DCMPL:
		case Opcodes.LCMP:
			pop(4);
			push(Opcodes.INTEGER);
			break;
		case Opcodes.FCONST_0:
		case Opcodes.FCONST_1:
		case Opcodes.FCONST_2:
			push(Opcodes.FLOAT);
			break;
		case Opcodes.FNEG:
		case Opcodes.I2F:
			pop(1);
			push(Opcodes.FLOAT);
			break;
		case Opcodes.D2F:
		case Opcodes.FADD:
		case Opcodes.FALOAD:
		case Opcodes.FDIV:
		case Opcodes.FMUL:
		case Opcodes.FREM:
		case Opcodes.FSUB:
		case Opcodes.L2F:
			pop(2);
			push(Opcodes.FLOAT);
			break;
		case Opcodes.LCONST_0:
		case Opcodes.LCONST_1:
			push(Opcodes.LONG);
			push(Opcodes.TOP);
			break;
		case Opcodes.F2L:
		case Opcodes.I2L:
			pop(1);
			push(Opcodes.LONG);
			push(Opcodes.TOP);
			break;
		case Opcodes.D2L:
		case Opcodes.LALOAD:
		case Opcodes.LNEG:
			pop(2);
			push(Opcodes.LONG);
			push(Opcodes.TOP);
			break;
		case Opcodes.LSHL:
		case Opcodes.LSHR:
		case Opcodes.LUSHR:
			pop(3);
			push(Opcodes.LONG);
			push(Opcodes.TOP);
			break;
		case Opcodes.LADD:
		case Opcodes.LAND:
		case Opcodes.LDIV:
		case Opcodes.LMUL:
		case Opcodes.LOR:
		case Opcodes.LREM:
		case Opcodes.LSUB:
		case Opcodes.LXOR:
			pop(4);
			push(Opcodes.LONG);
			push(Opcodes.TOP);
			break;
		case Opcodes.DCONST_0:
		case Opcodes.DCONST_1:
			push(Opcodes.DOUBLE);
			push(Opcodes.TOP);
			break;
		case Opcodes.F2D:
		case Opcodes.I2D:
			pop(1);
			push(Opcodes.DOUBLE);
			push(Opcodes.TOP);
			break;
		case Opcodes.DALOAD:
		case Opcodes.DNEG:
		case Opcodes.L2D:
			pop(2);
			push(Opcodes.DOUBLE);
			push(Opcodes.TOP);
			break;
		case Opcodes.DADD:
		case Opcodes.DDIV:
		case Opcodes.DMUL:
		case Opcodes.DREM:
		case Opcodes.DSUB:
			pop(4);
			push(Opcodes.DOUBLE);
			push(Opcodes.TOP);
			break;
		case Opcodes.ACONST_NULL:
			push(Opcodes.NULL);
			break;
		case Opcodes.AALOAD:
			pop(1);
			t1 = pop();
			push(Type.getType(((String) t1).substring(1)));
			break;
		case Opcodes.DUP:
			t1 = pop();
			push(t1);
			push(t1);
			break;
		case Opcodes.DUP_X1:
			t1 = pop();
			t2 = pop();
			push(t1);
			push(t2);
			push(t1);
			break;
		case Opcodes.DUP_X2:
			t1 = pop();
			t2 = pop();
			t3 = pop();
			push(t1);
			push(t3);
			push(t2);
			push(t1);
			break;
		case Opcodes.DUP2:
			t1 = pop();
			t2 = pop();
			push(t2);
			push(t1);
			push(t2);
			push(t1);
			break;
		case Opcodes.DUP2_X1:
			t1 = pop();
			t2 = pop();
			t3 = pop();
			push(t2);
			push(t1);
			push(t3);
			push(t2);
			push(t1);
			break;
		case Opcodes.DUP2_X2:
			t1 = pop();
			t2 = pop();
			t3 = pop();
			t4 = pop();
			push(t2);
			push(t1);
			push(t4);
			push(t3);
			push(t2);
			push(t1);
			break;
		case Opcodes.SWAP:
			t1 = pop();
			t2 = pop();
			push(t1);
			push(t2);
			break;
		default:
			throw new IllegalArgumentException();
		}
		mv.visitInsn(opcode);
	}

	@Override
	public void visitIntInsn(final int opcode, final int operand) {
		switch (opcode) {
		case Opcodes.BIPUSH:
		case Opcodes.SIPUSH:
			push(Opcodes.INTEGER);
			break;
		case Opcodes.NEWARRAY:
			pop(1);
			switch (operand) {
			case Opcodes.T_BOOLEAN:
				push("[Z");
				break;
			case Opcodes.T_CHAR:
				push("[C");
				break;
			case Opcodes.T_FLOAT:
				push("[F");
				break;
			case Opcodes.T_DOUBLE:
				push("[D");
				break;
			case Opcodes.T_BYTE:
				push("[B");
				break;
			case Opcodes.T_SHORT:
				push("[S");
				break;
			case Opcodes.T_INT:
				push("[I");
				break;
			case Opcodes.T_LONG:
				push("[J");
				break;
			default:
				throw new IllegalArgumentException();
			}
			break;
		default:
			throw new IllegalArgumentException();
		}
		mv.visitIntInsn(opcode, operand);
	}

	@Override
	public void visitVarInsn(final int opcode, final int var) {
		final Object t;
		switch (opcode) {
		case Opcodes.ALOAD:
			push(get(var));
			break;
		case Opcodes.ILOAD:
			push(Opcodes.INTEGER);
			break;
		case Opcodes.FLOAD:
			push(Opcodes.FLOAT);
			break;
		case Opcodes.LLOAD:
			push(Opcodes.LONG);
			push(Opcodes.TOP);
			break;
		case Opcodes.DLOAD:
			push(Opcodes.DOUBLE);
			push(Opcodes.TOP);
			break;
		case Opcodes.ASTORE:
		case Opcodes.ISTORE:
		case Opcodes.FSTORE:
			t = pop();
			set(var, t);
			break;
		case Opcodes.LSTORE:
		case Opcodes.DSTORE:
			pop(1);
			t = pop();
			set(var, t);
			set(var + 1, Opcodes.TOP);
			break;
		default:
			throw new IllegalArgumentException();
		}
		mv.visitVarInsn(opcode, var);
	}

	@Override
	public void visitTypeInsn(final int opcode, final String type) {
		switch (opcode) {
		case Opcodes.NEW:
			final Label label = new Label();
			mv.visitLabel(label);
			push(label);
			break;
		case Opcodes.ANEWARRAY:
			pop(1);
			push('[' + Type.getObjectType(type).getDescriptor());
			break;
		case Opcodes.CHECKCAST:
			pop(1);
			push(type);
			break;
		case Opcodes.INSTANCEOF:
			pop(1);
			push(Opcodes.INTEGER);
			break;
		default:
			throw new IllegalArgumentException();
		}
		mv.visitTypeInsn(opcode, type);
	}

	@Override
	public void visitFieldInsn(final int opcode, final String owner,
			final String name, final String desc) {
		final Type t = Type.getType(desc);
		switch (opcode) {
		case Opcodes.PUTSTATIC:
			pop(t);
			break;
		case Opcodes.PUTFIELD:
			pop(t);
			pop(1);
			break;
		case Opcodes.GETSTATIC:
			push(t);
			break;
		case Opcodes.GETFIELD:
			pop(1);
			push(t);
			break;
		default:
			throw new IllegalArgumentException();
		}
		mv.visitFieldInsn(opcode, owner, name, desc);
	}

	@Override
	public void visitMethodInsn(final int opcode, final String owner,
			final String name, final String desc) {
		for (final Type t : Type.getArgumentTypes(desc)) {
			pop(t);
		}
		if (opcode != Opcodes.INVOKESTATIC) {
			final Object target = pop();
			if (target == Opcodes.UNINITIALIZED_THIS) {
				replace(Opcodes.UNINITIALIZED_THIS, this.owner);
			} else if (target instanceof Label) {
				replace(target, owner);
			}
		}
		push(Type.getReturnType(desc));

		mv.visitMethodInsn(opcode, owner, name, desc);
	}

	@Override
	public void visitInvokeDynamicInsn(final String name, final String desc,
			final Handle bsm, final Object... bsmArgs) {
		for (final Type t : Type.getArgumentTypes(desc)) {
			pop(t);
		}
		push(Type.getReturnType(desc));

		mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
	}

	@Override
	public void visitLdcInsn(final Object cst) {
		if (cst instanceof Integer) {
			push(Opcodes.INTEGER);
		} else if (cst instanceof Float) {
			push(Opcodes.FLOAT);
		} else if (cst instanceof Long) {
			push(Opcodes.LONG);
			push(Opcodes.TOP);
		} else if (cst instanceof Double) {
			push(Opcodes.DOUBLE);
			push(Opcodes.TOP);
		} else if (cst instanceof String) {
			push("java/lang/String");
		} else if (cst instanceof Type) {
			push("java/lang/Class");
		} else {
			throw new IllegalArgumentException();
		}
		mv.visitLdcInsn(cst);
	}

	@Override
	public void visitJumpInsn(final int opcode, final Label label) {
		switch (opcode) {
		case Opcodes.GOTO:
			break;
		case Opcodes.IFEQ:
		case Opcodes.IFNE:
		case Opcodes.IFLT:
		case Opcodes.IFGE:
		case Opcodes.IFGT:
		case Opcodes.IFLE:
		case Opcodes.IFNULL:
		case Opcodes.IFNONNULL:
			pop(1);
			break;
		case Opcodes.IF_ICMPEQ:
		case Opcodes.IF_ICMPNE:
		case Opcodes.IF_ICMPLT:
		case Opcodes.IF_ICMPGE:
		case Opcodes.IF_ICMPGT:
		case Opcodes.IF_ICMPLE:
		case Opcodes.IF_ACMPEQ:
		case Opcodes.IF_ACMPNE:
			pop(2);
			break;
		default:
			throw new IllegalArgumentException();
		}
		mv.visitJumpInsn(opcode, label);
	}

	@Override
	public void visitIincInsn(final int var, final int increment) {
		set(var, Opcodes.INTEGER);
		mv.visitIincInsn(var, increment);
	}

	@Override
	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label... labels) {
		pop(1);
		mv.visitTableSwitchInsn(min, max, dflt, labels);
	}

	@Override
	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
		pop(1);
		mv.visitLookupSwitchInsn(dflt, keys, labels);
	}

	@Override
	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		pop(dims);
		push(desc);
		mv.visitMultiANewArrayInsn(desc, dims);
	}

	private void push(final Object type) {
		stack = ensureSize(stack, stackSize + 1);
		stack[stackSize] = type;
		stackSize++;
	}

	private void push(final Type type) {
		switch (type.getSort()) {
		case Type.VOID:
			break;
		case Type.BOOLEAN:
		case Type.BYTE:
		case Type.CHAR:
		case Type.INT:
		case Type.SHORT:
			push(Opcodes.INTEGER);
			break;
		case Type.FLOAT:
			push(Opcodes.FLOAT);
			break;
		case Type.LONG:
			push(Opcodes.LONG);
			push(Opcodes.TOP);
			break;
		case Type.DOUBLE:
			push(Opcodes.DOUBLE);
			push(Opcodes.TOP);
			break;
		case Type.ARRAY:
		case Type.OBJECT:
			push(type.getInternalName());
			break;
		default:
			throw new AssertionError(type);
		}
	}

	private Object pop() {
		stackSize--;
		assertValidFrames(stackSize);
		return stack[stackSize];
	}

	private void pop(final int count) {
		stackSize -= count;
		assertValidFrames(stackSize);
	}

	private void assertValidFrames(final int stackSize) {
		if (stackSize < 0) {
			throw new IllegalStateException(
					"Missing or invalid stackmap frames.");
		}
	}

	private void pop(final Type type) {
		pop(type.getSize());
	}

	private void set(final int pos, final Object type) {
		local = ensureSize(local, pos + 1);
		// fill gaps:
		for (int i = localSize; i < pos; i++) {
			local[i] = Opcodes.TOP;
		}
		localSize = Math.max(localSize, pos + 1);
		local[pos] = type;
	}

	private void set(final int pos, final Type type) {
		switch (type.getSort()) {
		case Type.BOOLEAN:
		case Type.BYTE:
		case Type.CHAR:
		case Type.INT:
		case Type.SHORT:
			set(pos, Opcodes.INTEGER);
			break;
		case Type.FLOAT:
			set(pos, Opcodes.FLOAT);
			break;
		case Type.LONG:
			set(pos, Opcodes.LONG);
			set(pos + 1, Opcodes.TOP);
			break;
		case Type.DOUBLE:
			set(pos, Opcodes.DOUBLE);
			set(pos + 1, Opcodes.TOP);
			break;
		case Type.ARRAY:
		case Type.OBJECT:
			set(pos, type.getInternalName());
			break;
		default:
			throw new AssertionError(type);
		}
	}

	private Object get(final int pos) {
		if (localSize <= pos) {
			throw new IllegalStateException(
					"Missing or invalid stackmap frames.");
		}
		return local[pos];
	}

	private Object[] ensureSize(final Object[] array, final int size) {
		if (array.length >= size) {
			return array;
		}
		int newLength = array.length;
		while (newLength < size) {
			newLength *= 2;
		}
		final Object[] newArray = new Object[newLength];
		System.arraycopy(array, 0, newArray, 0, array.length);
		return newArray;
	}

	/**
	 * Expand double word types into two slots.
	 */
	private int expand(final Object[] source, final int size,
			final Object[] target) {
		int targetIdx = 0;
		for (int sourceIdx = 0; sourceIdx < size; sourceIdx++) {
			final Object type = source[sourceIdx];
			target[targetIdx++] = type;
			if (type == Opcodes.LONG || type == Opcodes.DOUBLE) {
				target[targetIdx++] = Opcodes.TOP;
			}
		}
		return targetIdx;
	}

	/**
	 * Reduce double word types into a single slot.
	 */
	private int reduce(final Object[] source, final int size,
			final Object[] target) {
		int targetIdx = 0;
		for (int sourceIdx = 0; sourceIdx < size; sourceIdx++) {
			final Object type = source[sourceIdx];
			target[targetIdx++] = type;
			if (type == Opcodes.LONG || type == Opcodes.DOUBLE) {
				sourceIdx++;
			}
		}
		return targetIdx;
	}

	/**
	 * Replaces a type in the locals and on the stack. This is used for
	 * uninitialized objects.
	 * 
	 * @param oldtype
	 *            type to replace
	 * @param newtype
	 *            replacement type
	 */
	private void replace(final Object oldtype, final Object newtype) {
		for (int i = 0; i < localSize; i++) {
			if (oldtype.equals(local[i])) {
				local[i] = newtype;
			}
		}
		for (int i = 0; i < stackSize; i++) {
			if (oldtype.equals(stack[i])) {
				stack[i] = newtype;
			}
		}
	}

}
