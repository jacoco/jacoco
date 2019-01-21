/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * Recorder of method events for test verification.
 */
public class MethodRecorder {

	private final Printer printer;
	private final MethodVisitor visitor;

	public MethodRecorder() {
		printer = new Textifier();
		visitor = new TraceMethodVisitor(printer);
	}

	public Printer getPrinter() {
		return printer;
	}

	public MethodVisitor getVisitor() {
		return visitor;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MethodRecorder)) {
			return false;
		}
		MethodRecorder that = (MethodRecorder) obj;
		return printer.getText().equals(that.printer.getText());
	}

	@Override
	public int hashCode() {
		return printer.getText().hashCode();
	}

	@Override
	public String toString() {
		StringWriter buffer = new StringWriter();
		PrintWriter writer = new PrintWriter(buffer);
		printer.print(writer);
		writer.flush();
		return buffer.toString();
	}
}
