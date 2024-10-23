/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Ivo Šmíd & ant-contrib contributors
 *
 *******************************************************************************/
package org.jacoco.ant.task;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Property;

// Forked from net.sf.antcontrib.property.AbstractPropertySetterTask
public abstract class AbstractPropertySetterTask extends Task {
	private boolean override;
	private String property;

	public AbstractPropertySetterTask() {
	}

	public void setOverride(boolean override) {
		this.override = override;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	protected void validate() {
		if (this.property == null) {
			throw new BuildException("You must specify a property to set.");
		}
	}

	protected final void setPropertyValue(String value) {
		if (value != null) {
			if (this.override) {
				if (this.getProject().getUserProperty(this.property) == null) {
					this.getProject().setProperty(this.property, value);
				} else {
					this.getProject().setUserProperty(this.property, value);
				}
			} else {
				Property p = (Property) this.project.createTask("property");
				p.setName(this.property);
				p.setValue(value);
				p.execute();
			}
		}

	}
}
