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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * Recorder of method events for test verification.
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public class MethodRecorder extends TraceMethodVisitor {

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MethodRecorder)) {
			return false;
		}
		MethodRecorder that = (MethodRecorder) obj;
		return text.equals(that.text);
	}

	@Override
	public int hashCode() {
		return text.hashCode();
	}

	@Override
	public String toString() {
		StringWriter buffer = new StringWriter();
		PrintWriter printer = new PrintWriter(buffer);
		print(printer);
		printer.flush();
		return buffer.toString();
	}
}
