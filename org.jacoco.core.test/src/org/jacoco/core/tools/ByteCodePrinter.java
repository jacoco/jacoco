/*******************************************************************************
 * Copyright (c) 2009, 2015 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.tools;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Stack;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * Debug helper that dumps byte code of a generated class so we can see why it
 * fails.
 * <p>
 * DO NOT DELETE. Even if currently unused as we don't always dump just add this
 * as needed.
 * 
 * @author Omer Azmon
 */
public class ByteCodePrinter {
	/**
	 * Print a formatted dump of a class byte code
	 * 
	 * @param out
	 *            destination stream
	 * @param className
	 *            the name of the class (for printing purposes only)
	 * @param bytecode
	 *            the byte code to dump
	 */
	public static void print(PrintStream out, String className, byte[] bytecode) {
		ClassParser cp = new ClassParser(new ByteArrayInputStream(bytecode),
				className);
		JavaClass clazz;
		try {
			clazz = cp.parse();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		out.println(clazz);
		Method[] methods = clazz.getMethods();
		for (int i = 0; i < methods.length; i++) {
			out.println(methods[i]);
			Code code = methods[i].getCode();
			if (code != null) // Non-abstract method
				out.println(code);
		}
	}

	private static Printer printer = new Textifier();
	private static TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(
			printer);

	public static String insnToString(AbstractInsnNode insn) {
		insn.accept(traceMethodVisitor);
		StringWriter sw = new StringWriter();
		printer.print(new PrintWriter(sw));
		printer.getText().clear();
		return sw.toString();
	}

	public static void printLastXInstructions(final AbstractInsnNode insn,
			final int count) {
		final Stack<String> stack = new Stack<String>();
		stack.push(insnToString(insn));
		AbstractInsnNode previous = insn.getPrevious();
		for (int i = 0; previous != null && i < count; i++, previous = previous
				.getPrevious()) {
			stack.push(insnToString(previous));
		}
		System.out.println("Insrruction dump:");
		System.out.println(insn.getClass().getName());
		while (!stack.isEmpty()) {
			System.out.print(stack.pop());
		}
		System.out.println();
	}

}
