/*******************************************************************************
 *  Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Omer Azmon - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.ebigo.internal.util;

/**
 * A utility class for method that validate parameters.
 * 
 * @author Omer Azmon
 *
 */
public final class ValidationUtils {
	private ValidationUtils() {
	}

	/**
	 * Validate that an value is not {@code null}.
	 * 
	 * @param name
	 *            the value name that will be used in the message
	 * @param value
	 *            the value to validate that it is not {@code null}
	 * @throws IllegalArgumentException
	 *             if it is null
	 */
	public static void validateNotNull(final String name, final Object value) {
		if (value == null) {
			throw new IllegalArgumentException(name + " is null");
		}
	}
}