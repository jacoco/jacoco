/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.ant;

import org.apache.tools.ant.BuildException;

/**
 * Ant task that will unpack the coverage agent jar and generate the JVM options
 * required to use it
 */
public class AgentTask extends AbstractCoverageTask {

	private String property;

	/**
	 * Sets the name of the property to hold the agent JVM options
	 *
	 * @param property
	 *            Name of the property to be populated
	 */
	public void setProperty(final String property) {
		this.property = property;
	}

	/**
	 * Unpacks a private copy of the JaCoCo agent and populates
	 * <code>property</code> with the JVM arguments required to use it. The
	 * value set into the property is only valid for the lifetime of the current
	 * JVM. The agent jar will be removed on termination of the JVM.
	 */
	@Override
	public void execute() throws BuildException {
		if (property == null || property.length() == 0) {
			throw new BuildException("Property is mandatory", getLocation());
		}
		final String jvmArg = isEnabled() ? getLaunchingArgument() : "";

		getProject().setNewProperty(property, jvmArg);
	}
}
