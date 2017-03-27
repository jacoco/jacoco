/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.internal.flow;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.util.Textifiable;
import org.objectweb.asm.util.TraceClassVisitor;

public class CharacterRangeTableAttribute extends Attribute implements
		Textifiable {

	// TODO(Godin): add others?
	public static final int CRT_BRANCH_TRUE = 0x0080;
	public static final int CRT_BRANCH_FALSE = 0x0100;

	public static final int CRT_BRANCH_FLAGS = CRT_BRANCH_TRUE
			| CRT_BRANCH_FALSE;

	public final Entry[] entries;

	public static class Entry {
		public final Label startLabel;
		public final Label endLabel;
		public final int charStart;
		public final int charEnd;
		public final int flags;

		Entry(Label startLabel, Label endLabel, int charStart, int charEnd,
				int flags) {
			this.startLabel = startLabel;
			this.endLabel = endLabel;
			this.charStart = charStart;
			this.charEnd = charEnd;
			this.flags = flags;
		}
	}

	public CharacterRangeTableAttribute() {
		this(new Entry[0]);
	}

	private CharacterRangeTableAttribute(Entry[] entries) {
		super("CharacterRangeTable");
		this.entries = entries;
	}

	@Override
	public boolean isUnknown() {
		return false;
	}

	@Override
	protected Attribute read(ClassReader cr, int off, int len, char[] buf,
			int codeOff, Label[] labels) {
		int length = cr.readShort(off);
		Entry[] entries = new Entry[length];
		for (int i = 0; i < length; i++) {
			int entryOff = off + 2 + (i * 14);
			int startPc = cr.readShort(entryOff);
			int endPc = cr.readShort(entryOff + 2);
			int charStart = cr.readInt(entryOff + 4);
			int charEnd = cr.readInt(entryOff + 8);
			int flags = cr.readShort(entryOff + 12);
			entries[i] = new Entry(getLabel(startPc, labels), getLabel(endPc,
					labels), charStart, charEnd, flags);
		}
		return new CharacterRangeTableAttribute(entries);
	}

	private static Label getLabel(int offset, Label[] labels) {
		if (labels[offset] == null) {
			labels[offset] = new Label();
		}
		return labels[offset];
	}

	@Override
	protected ByteVector write(ClassWriter cw, byte[] code, int len,
			int maxStack, int maxLocals) {
		ByteVector bv = new ByteVector();
		bv.putShort(entries.length);
		for (Entry entry : entries) {
			bv.putShort(entry.startLabel.getOffset());
			bv.putShort(entry.endLabel.getOffset());
			bv.putInt(entry.charEnd);
			bv.putInt(entry.charStart);
			bv.putShort(entry.flags);
		}
		return bv;
	}

	public void textify(StringBuffer buf, Map<Label, String> labelNames) {
		buf.append(":\n");
		for (Entry entry : entries) {
			if ((entry.flags & CRT_BRANCH_FLAGS) != 0) {
				buf.append("   ").append(labelNames.get(entry.startLabel))
						.append(' ').append(decodePos(entry.charStart))
						.append(' ').append(decodePos(entry.charEnd))
						.append(' ').append(decodeFlags(entry.flags))
						.append('\n');
			}
		}
	}

	public static String decodePos(int pos) {
		int line = pos >> 10;
		int column = pos & 0x3ff;
		return line + ":" + column;
	}

	private static String decodeFlags(int flags) {
		String types = "";
		if ((flags & CRT_BRANCH_TRUE) != 0) {
			types += " branch-true";
		}
		if ((flags & CRT_BRANCH_FALSE) != 0) {
			types += " branch-false";
		}
		return types;
	}

	// FIXME(Godin): for quick tests, should be removed
	public static void main(String[] args) throws IOException {
		ClassReader cr = new ClassReader(new FileInputStream(
				"/tmp/jacoco/Example.class"));
		ClassVisitor cv = new TraceClassVisitor(new PrintWriter(System.out));
		cr.accept(cv, new Attribute[] { new CharacterRangeTableAttribute() }, 0);
	}

}
