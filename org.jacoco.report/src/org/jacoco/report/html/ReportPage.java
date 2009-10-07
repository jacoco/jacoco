/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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
package org.jacoco.report.html;

import java.io.IOException;

import org.jacoco.core.JaCoCo;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.resources.Resources;
import org.jacoco.report.html.resources.Styles;

/**
 * Base class for HTML page generators. It renders the page skeleton with the
 * breadcrumb, the title and the footer.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public abstract class ReportPage implements IReportVisitor, ICoverageTableItem {

	private final ReportPage parent;

	/** output folder for this node */
	protected final ReportOutputFolder outputFolder;

	/** context for this report */
	protected final IHTMLReportContext context;

	private ICoverageNode node;

	/**
	 * Creates a new report page.
	 * 
	 * @param node
	 *            corresponding node
	 * @param parent
	 *            optional hierarchical parent
	 * @param baseFolder
	 *            base folder to create this report page relative to
	 * @param context
	 *            settings context
	 */
	protected ReportPage(final ICoverageNode node, final ReportPage parent,
			final ReportOutputFolder baseFolder,
			final IHTMLReportContext context) {
		this.node = node;
		this.parent = parent;
		this.context = context;
		this.outputFolder = getFolder(baseFolder);
	}

	public void visitEnd(final ISourceFileLocator sourceFileLocator)
			throws IOException {
		renderDocument(sourceFileLocator);
		this.node = node.getPlainCopy();
	}

	private void renderDocument(final ISourceFileLocator sourceFileLocator)
			throws IOException {
		final HTMLDocument doc = new HTMLDocument(outputFolder
				.createFile(getFileName()), context.getOutputEncoding());
		head(doc.head());
		final HTMLElement body = doc.body();
		breadcrumb(body.div(Styles.BREADCRUMB), outputFolder, this);
		body.h1().text(getLabel());
		content(body, sourceFileLocator);
		footer(body);
		doc.close();
	}

	private void head(final HTMLElement head) throws IOException {
		head.meta("Content-Type", "text/html;charset=UTF-8");
		head.link("stylesheet", context.getResources().getLink(outputFolder,
				Resources.STYLESHEET), "text/css");
		head.link("shortcut icon", context.getResources().getLink(outputFolder,
				"session.gif"), "image/gif");
		head.title().text(getLabel());
	}

	private void breadcrumb(final HTMLElement body,
			final ReportOutputFolder base, final ReportPage current)
			throws IOException {
		if (parent != null) {
			parent.breadcrumb(body, base, current);
			body.text(" > ");
		}
		final String style = Resources.getElementStyle(node.getElementType());
		if (this == current) {
			body.span(style).text(getLabel());
		} else {
			body.a(getLink(base), style).text(getLabel());
		}
	}

	private void footer(final HTMLElement body) throws IOException {
		final HTMLElement footer = body.div(Styles.FOOTER);
		final HTMLElement versioninfo = footer.div(Styles.VERSIONINFO);
		versioninfo.text("Created with ");
		versioninfo.a(JaCoCo.HOMEURL).text("JaCoCo");
		versioninfo.text(" ").text(JaCoCo.VERSION);
		footer.text(context.getFooterText());
	}

	/**
	 * Creates the actual content of the page.
	 * 
	 * @param body
	 *            body tag of the page
	 * @param sourceFileLocator
	 *            locator for source file content in this context
	 * 
	 * @throws IOException
	 *             in case of IO problems with the report writer
	 */
	protected abstract void content(final HTMLElement body,
			final ISourceFileLocator sourceFileLocator) throws IOException;

	/**
	 * Specifies the local file name of this page.
	 * 
	 * @return local file name
	 */
	protected abstract String getFileName();

	/**
	 * Creates the output folder relative to the given base for this report
	 * page. The method may decide to simply return the base folder itself.
	 * 
	 * @param base
	 *            base folder
	 * @return folder to create this page in
	 */
	protected abstract ReportOutputFolder getFolder(ReportOutputFolder base);

	// === ICoverageTableItem ===

	public String getLabel() {
		return node.getName();
	}

	public ICoverageNode getNode() {
		return node;
	}

	public final String getLink(final ReportOutputFolder base) {
		return outputFolder.getLink(base, getFileName());
	}

}
