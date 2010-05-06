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
 * $Id: $
 *******************************************************************************/
package org.jacoco.core;

/**
 * Static Meta information about JaCoCo.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public interface JaCoCo {

	/** Qualified build version of the JaCoCo core library. */
	public static final String VERSION = "@qualified.bundle.version@";

	/** Absolute URL of the current JaCoCo home page */
	public static final String HOMEURL = "@jacoco.home.url@";

}
