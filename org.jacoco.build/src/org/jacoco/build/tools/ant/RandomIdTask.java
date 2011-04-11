/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.build.tools.ant;

import java.util.Random;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * This task creates a random identifier and loads it into a property.
 */
public class RandomIdTask extends Task {

	private String name;

	/**
	 * Sets the name of the property where the identifier should be stored.
	 * 
	 * @param name
	 *            property name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public void execute() throws BuildException {
		final int id = Math.abs(new Random().nextInt());
		final String idstr = Integer.toString(id, Character.MAX_RADIX);
		getProject().setNewProperty(name, idstr);
	}

}
