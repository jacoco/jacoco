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
package org.jacoco.core.data;

import java.io.PrintStream;

/**
 * {@link IExecutionDataOutput} implementation that dumps a textual
 * representation of coverage data.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ExecutionDataDumper implements IExecutionDataOutput {

	private final PrintStream out;

	/**
	 * Dumps coverage data to {@link System#out}.
	 */
	public ExecutionDataDumper() {
		this(System.out);
	}

	/**
	 * Dumps coverage data to the given stream.
	 * 
	 * @param out
	 *            dump output
	 */
	public ExecutionDataDumper(final PrintStream out) {
		this.out = out;
	}

	public void classExecution(final long id, final boolean[][] blockdata) {
		out.print("class coverage ");
		out.print(" id=");
		out.println(Long.toHexString(id));
	}

}
