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

/**
 * Allow core to generate messages without knowing the implementation, such as,
 * Maven, Ant, etc.
 */
public interface LoggingBridge {

	/**
	 * Generate an info level message
	 * 
	 * @param msg
	 *            the message
	 */
	public void info(String msg);

	/**
	 * Generate an warning level message
	 * 
	 * @param msg
	 *            the message
	 */
	public void warning(String msg);

	/**
	 * Generate an severe (aka error) level message
	 * 
	 * @param msg
	 *            the message
	 */
	public void severe(String msg);

}