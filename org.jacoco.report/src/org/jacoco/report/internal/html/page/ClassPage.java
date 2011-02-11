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
package org.jacoco.report.internal.html.page;

import java.io.IOException;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.ISourceNode;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.IHTMLReportContext;
import org.jacoco.report.internal.html.ILinkable;
import org.jacoco.report.internal.html.resources.Styles;
import org.jacoco.report.internal.html.table.ITableItem;

/**
 * Page showing coverage information for a class as a table of methods. The
 * methods are linked to the corresponding source file.
 */
public class ClassPage extends TablePage<IClassCoverage> {

	private class MethodItem implements ITableItem {

		private final IMethodCoverage node;

		MethodItem(final IMethodCoverage node) {
			this.node = node;
		}

		public String getLinkLabel() {
			return context.getLanguageNames().getMethodName(
					ClassPage.this.getNode().getName(), node.getName(),
					node.getDesc(), node.getSignature());
		}

		public String getLinkStyle() {
			return Styles.EL_METHOD;
		}

		public String getLink(final ReportOutputFolder base) {
			if (sourcePage == null) {
				return null;
			}
			final String link = sourcePage.getLink(base);
			final int first = node.getFirstLine();
			return first != ISourceNode.UNKNOWN_LINE ? link + "#L" + first
					: link;
		}

		public ICoverageNode getNode() {
			return node;
		}

	}

	private final ILinkable sourcePage;

	/**
	 * Creates a new visitor in the given context.
	 * 
	 * @param classNode
	 * @param parent
	 * @param sourcePage
	 *            corresponding source page or <code>null</code>
	 * @param folder
	 * @param context
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
			addItem(new MethodItem(m));
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

}
