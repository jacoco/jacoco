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

import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
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
public abstract class ReportPage implements IReportVisitor {

	private final ReportPage parent;

	/** node type */
	protected final ElementType type;

	/** node name */
	protected String name;

	/** output folder for this node */
	protected final ReportOutputFolder outputFolder;

	/** static resources for the overall report */
	protected final Resources resources;

	private ICoverageNode node;

	/**
	 * 
	 * @param type
	 * @param name
	 * @param parent
	 * @param outputFolder
	 * @param resources
	 */
	protected ReportPage(final ICoverageNode.ElementType type,
			final String name, final ReportPage parent,
			final ReportOutputFolder outputFolder, final Resources resources) {
		this.type = type;
		this.name = name;
		this.parent = parent;
		this.outputFolder = outputFolder;
		this.resources = resources;
	}

	public void visitEnd(final ICoverageNode node,
			final ISourceFileLocator sourceFileLocator) throws IOException {
		this.node = node;
		renderDocument(sourceFileLocator);
	}

	private void renderDocument(final ISourceFileLocator sourceFileLocator)
			throws IOException {
		final HTMLDocument doc = new HTMLDocument(outputFolder
				.createFile(getFileName()));
		head(doc.head());
		final HTMLElement body = doc.body();
		breadcrumb(body.div(Styles.BREADCRUMB), outputFolder, this);
		body.h1().text(getNode().getName());
		content(body, sourceFileLocator);
		footer(body);
		doc.close();
	}

	private void head(final HTMLElement head) throws IOException {
		head.meta("Content-Type", "text/html;charset=UTF-8");
		head.link("stylesheet", resources.getLink(outputFolder,
				Resources.STYLESHEET), "text/css");
		head.link("shortcut icon", resources.getLink(outputFolder,
				"session.gif"), "image/gif");
		head.title().text(name);
	}

	private void breadcrumb(final HTMLElement body,
			final ReportOutputFolder base, final ReportPage current)
			throws IOException {
		if (parent != null) {
			parent.breadcrumb(body, base, current);
			body.text(" > ");
		}
		if (this == current) {
			body.span(Resources.getElementStyle(type)).text(name);
		} else {
			body.a(getLink(base), Resources.getElementStyle(type)).text(name);
		}
	}

	private void footer(final HTMLElement body) throws IOException {
		// TODO
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
	 * Returns the node that belong to this page. The value is not available
	 * before {@link #visitEnd(ICoverageNode, ISourceFileLocator)} has been
	 * called.
	 * 
	 * @return corresponding node or <code>null</code>
	 */
	public ICoverageNode getNode() {
		return node;
	}

	/**
	 * Calculates a relative link to this page from the given base.
	 * 
	 * @param base
	 *            base folder from where the link is created
	 * @return relative link to this page
	 */
	public final String getLink(final ReportOutputFolder base) {
		return outputFolder.getLink(base, getFileName());
	}

	/**
	 * Specifies the local file name of this page.
	 * 
	 * @return local file name
	 */
	protected abstract String getFileName();

}
