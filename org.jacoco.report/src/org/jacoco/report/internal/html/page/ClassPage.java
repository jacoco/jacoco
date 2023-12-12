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
package org.jacoco.report.internal.html.page;

import java.io.IOException;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.IHTMLReportContext;
import org.jacoco.report.internal.html.ILinkable;

/**
 * Page showing coverage information for a class as a table of methods. The
 * methods are linked to the corresponding source file.
 */
public class ClassPage extends TablePage<IClassCoverage> {

	private final ILinkable sourcePage;

	/**
	 * Creates a new visitor in the given context.
	 *
	 * @param classNode
	 *            coverage data for this class
	 * @param parent
	 *            optional hierarchical parent
	 * @param sourcePage
	 *            corresponding source page or <code>null</code>
	 * @param folder
	 *            base folder to create this page in
	 * @param context
	 *            settings context
	 */
	public ClassPage(final IClassCoverage classNode, final ReportPage parent,
			final ILinkable sourcePage, final ReportOutputFolder folder,
			final IHTMLReportContext context) {
		super(classNode, parent, folder, context);
		this.sourcePage = sourcePage;
		context.getIndexUpdate().addClass(this, classNode.getId());
	}

	@Override
	protected String getOnload() {
		return "initialSort(['breadcrumb'])";
	}

	@Override
	public void render() throws IOException {
		for (final IMethodCoverage m : getNode().getMethods()) {
			final String label = context.getLanguageNames().getMethodName(
					getNode().getName(), m.getName(), m.getDesc(),
					m.getSignature());
			addItem(new MethodItem(m, label, sourcePage));
		}
		super.render();
	}

	@Override
	protected String getFileName() {
		final String vmname = getNode().getName();
		final int pos = vmname.lastIndexOf('/');
		final String shortname = pos == -1 ? vmname : vmname.substring(pos + 1);
		return shortname + ".html";
	}

	@Override
	public String getLinkLabel() {
		return context.getLanguageNames().getClassName(getNode().getName(),
				getNode().getSignature(), getNode().getSuperName(),
				getNode().getInterfaceNames());
	}

	@Override
	protected void content(HTMLElement body) throws IOException {
		if (getNode().isNoMatch()) {
			body.p().text(
					"A different version of class was executed at runtime.");
		}

		if (getNode().getLineCounter().getTotalCount() == 0) {
			body.p().text(
					"Class files must be compiled with debug information to show line coverage.");
		}

		final String sourceFileName = getNode().getSourceFileName();
		if (sourceFileName == null) {
			body.p().text(
					"Class files must be compiled with debug information to link with source files.");

		} else if (sourcePage == null) {
			final String sourcePath;
			if (getNode().getPackageName().length() != 0) {
				sourcePath = getNode().getPackageName() + "/" + sourceFileName;
			} else {
				sourcePath = sourceFileName;
			}
			body.p().text("Source file \"" + sourcePath
					+ "\" was not found during generation of report.");
		}

		super.content(body);
	}

}
