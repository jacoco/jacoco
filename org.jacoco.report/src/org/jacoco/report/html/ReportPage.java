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
package org.jacoco.report.html;

import java.io.IOException;

import org.jacoco.core.JaCoCo;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.resources.Resources;
import org.jacoco.report.html.resources.Styles;

/**
 * Base class for HTML page generators. It renders the page skeleton with the
 * breadcrumb, the title and the footer. Every report page is part of a
 * hierarchy and has a parent page (except the root page).
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public abstract class ReportPage {

	private final ReportPage parent;

	/** output folder for this node */
	protected final ReportOutputFolder folder;

	/** context for this report */
	protected final IHTMLReportContext context;

	/**
	 * Creates a new report page.
	 * 
	 * @param parent
	 *            optional hierarchical parent
	 * @param folder
	 *            base folder to create this report in
	 * @param context
	 *            settings context
	 */
	protected ReportPage(final ReportPage parent,
			final ReportOutputFolder folder, final IHTMLReportContext context) {
		this.parent = parent;
		this.context = context;
		this.folder = folder;
	}

	/**
	 * Returns a relative link to this page that works from the given base
	 * folder.
	 * 
	 * @param base
	 *            folder where the link should be inserted
	 * @return relative link
	 */
	public final String getLink(final ReportOutputFolder base) {
		return folder.getLink(base, getFileName());
	}

	/**
	 * Renders the page content. This method must be called at most once.
	 * 
	 * @throws IOException
	 */
	public final void renderDocument() throws IOException {
		final HTMLDocument doc = new HTMLDocument(folder
				.createFile(getFileName()), context.getOutputEncoding());
		head(doc.head());
		body(doc.body());
		doc.close();
	}

	/**
	 * Fills the content of the 'head' element.
	 * 
	 * @param head
	 *            enclosing head element
	 * @throws IOException
	 *             in case of IO problems with the report writer
	 */
	protected void head(final HTMLElement head) throws IOException {
		head.meta("Content-Type", "text/html;charset=UTF-8");
		head.link("stylesheet", context.getResources().getLink(folder,
				Resources.STYLESHEET), "text/css");
		head.link("shortcut icon", context.getResources().getLink(folder,
				"session.gif"), "image/gif");
		head.title().text(getLabel());
	}

	/**
	 * Renders the content of the body element.
	 * 
	 * @param body
	 *            enclosing body element
	 * @throws IOException
	 *             in case of IO problems with the report writer
	 */
	protected void body(final HTMLElement body) throws IOException {
		final HTMLElement navigation = body.div(Styles.BREADCRUMB);
		infoLinks(navigation.span(Styles.RIGHT));
		breadcrumb(navigation, folder);
		body.h1().text(getLabel());
		content(body);
		footer(body);
	}

	private void infoLinks(final HTMLElement span) throws IOException {
		span.a(context.getInfoPageLink(folder), Styles.EL_SESSIONS).text(
				"Sessions");
	}

	private void breadcrumb(final HTMLElement div, final ReportOutputFolder base)
			throws IOException {
		breadcrumbParent(parent, div, base);
		div.span(getElementStyle()).text(getLabel());
	}

	private static void breadcrumbParent(final ReportPage page,
			final HTMLElement div, final ReportOutputFolder base)
			throws IOException {
		if (page != null) {
			breadcrumbParent(page.parent, div, base);
			final String style = page.getElementStyle();
			div.a(page.getLink(base), style).text(page.getLabel());
			div.text(" > ");
		}
	}

	private void footer(final HTMLElement body) throws IOException {
		final HTMLElement footer = body.div(Styles.FOOTER);
		final HTMLElement versioninfo = footer.span(Styles.RIGHT);
		versioninfo.text("Created with ");
		versioninfo.a(JaCoCo.HOMEURL).text("JaCoCo");
		versioninfo.text(" ").text(JaCoCo.VERSION);
		footer.text(context.getFooterText());
	}

	/**
	 * Specifies the local file name of this page.
	 * 
	 * @return local file name
	 */
	protected abstract String getFileName();

	/**
	 * Returns the display label used for the element represented on this page.
	 * 
	 * @return display label
	 */
	protected abstract String getLabel();

	/**
	 * The CSS style class that might be associated with this element when it is
	 * displayed in the header or in tables.
	 * 
	 * @return CSS style class for this element
	 */
	protected abstract String getElementStyle();

	/**
	 * Creates the actual content of the page.
	 * 
	 * @param body
	 *            body tag of the page
	 * @throws IOException
	 *             in case of IO problems with the report writer
	 */
	protected abstract void content(final HTMLElement body) throws IOException;

}
