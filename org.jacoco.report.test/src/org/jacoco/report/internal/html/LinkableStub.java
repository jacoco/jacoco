/*******************************************************************************
 * Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.report.internal.html;

import org.jacoco.report.internal.ReportOutputFolder;

/**
 * Stub implementation for {@link ILinkable}.
 */
public class LinkableStub implements ILinkable {

	protected final String link;
	private final String label;
	private final String style;

	public LinkableStub(String link, String label, String style) {
		this.link = link;
		this.label = label;
		this.style = style;
	}

	public String getLink(ReportOutputFolder base) {
		return link;
	}

	public String getLinkLabel() {
		return label;
	}

	public String getLinkStyle() {
		return style;
	}

}
