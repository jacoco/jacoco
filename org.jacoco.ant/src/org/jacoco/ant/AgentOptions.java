/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/
package org.jacoco.ant;

import java.io.File;

/**
 * Mirror of the AgentOptions class in the core project. Used to hold options
 * used to construct JVM argument
 * 
 * @author Brock Janiczak
 * @version $Revision: $
 */
public class AgentOptions {

	private boolean merge;
	private File file;
	private String exclClassLoader;

	/**
	 * Gets the location to write coverage execution data
	 * 
	 * @return Location to write coverage execution data
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Sets the location to write coverage execution data
	 * 
	 * @param file
	 *            Location to write coverage execution data
	 */
	public void setFile(final File file) {
		System.out.println("Setting file to: " + file);
		this.file = file;
	}

	/**
	 * Determine if results should be merged into existing coverage data if
	 * present
	 * 
	 * @return <code>true</code> if results should be merged if execution data
	 *         is already present
	 */
	public boolean isMerge() {
		return merge;
	}

	/**
	 * Merge execution coverage data if a coverage file is already present
	 * 
	 * @param merge
	 *            <code>true</code> to merge execution data
	 */
	public void setMerge(final boolean merge) {
		this.merge = merge;
	}

	/**
	 * Gets the value of the Excluded Class Loaders pattern
	 * 
	 * @return Wildcard pattern of class loaders to exclude
	 */
	public String getExclClassLoader() {
		return exclClassLoader;
	}

	/**
	 * Sets the value of the Excluded Class Loaders pattern
	 * 
	 * @param exclClassLoader
	 *            Wildcard pattern of class loaders to exclude
	 */
	public void setExclClassLoader(final String exclClassLoader) {
		this.exclClassLoader = exclClassLoader;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();

		sb.append("merge=");
		sb.append(merge);

		if (file != null) {
			sb.append(',');
			sb.append("file=");
			sb.append(file.toString());
		}

		if (exclClassLoader != null) {
			sb.append(',');
			sb.append("exclclassloader=");
			sb.append(exclClassLoader);
		}

		return sb.toString();
	}
}
