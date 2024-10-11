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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Reference;

// forked from net.sf.antcontrib.property.URLEncodeTask to support encoding parameter
public class URLEncodeTask extends AbstractPropertySetterTask {
	private String value;
	private Reference ref;
	private String encoding;

	public URLEncodeTask() {
	}

	public void setName(String name) {
		this.setProperty(name);
	}

	protected String getValue() {
		try {
			return encoding == null ? URLEncoder.encode(value)
					: URLEncoder.encode(value, encoding);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(
					"Invalid encoding=" + encoding + " specified", e);
		}
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getValue(Project p) {
		String val = this.getValue();
		if (this.ref != null) {
			val = this.ref.getReferencedObject(p).toString();
		}

		return val;
	}

	public void setLocation(File location) {
		this.setValue(location.getAbsolutePath());
	}

	public void setRefid(Reference ref) {
		this.ref = ref;
	}

	public String toString() {
		String value = this.getValue();
		return value == null ? "" : value;
	}

	protected void validate() {
		super.validate();
		if (this.getValue() == null && this.ref == null) {
			throw new BuildException(
					"You must specify value, location or refid with the name attribute",
					this.getLocation());
		}
	}

	public void execute() {
		this.validate();
		String val = this.getValue(this.getProject());
		this.setPropertyValue(val);
	}
}
