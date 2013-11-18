/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Keeping - initial implementation
 *
 *******************************************************************************/
package org.jacoco.cli;

import java.io.File;

import org.kohsuke.args4j.Option;

/**
 * Options for offline instrumentation task.
 */
public class InstrumentOptions {

	@Option(name = "-srcdir", required = true, usage = "directory containing the class files to be instrumented")
	private File srcdir;

	@Option(name = "-destdir", required = true, usage = "directory in which to place the instrumented class files")
	private File destdir;

	@Option(name = "-include", usage = "include filter expression (default: '*')")
	private String includeExpr = "*";

	@Option(name = "-exclude", usage = "exclude filter expression (default: '')")
	private String excludeExpr = "";

	/**
	 * Gets the directory containing the class files to be instrumented.
	 * 
	 * @return the input class file directory
	 */
	public File getSrcdir() {
		return srcdir;
	}

	/**
	 * Gets the directory into which to write instrumented class files.
	 * 
	 * @return the output class file directory
	 */
	public File getDestdir() {
		return destdir;
	}

	/**
	 * Gets the "include" filter expression.
	 * 
	 * @return the include filter expression
	 */
	public String getIncludeExpr() {
		return includeExpr;
	}

	/**
	 * Gets the "exclude" filter expression.
	 * 
	 * @return the exclude filter expression
	 */
	public String getExcludeExpr() {
		return excludeExpr;
	}

}
