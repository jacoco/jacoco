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
package org.jacoco.report.internal.html;

import java.io.IOException;

import org.jacoco.core.JaCoCo;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.internal.html.resources.Resources;
import org.jacoco.report.internal.html.resources.Styles;

/**
 * Base class for HTML page generators. It renders the page skeleton with the
 * breadcrumb, the title and the footer. Every report page is part of a
 * hierarchy and has a parent page (except the root page).
 * 
 * @author Marc R. Hoffmann
 * @version $qualified.bundle.version$
 */
public abstract class ReportPage implements ILinkable {

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
	 * Renders the page content. This method must be called at most once.
	 * 
	 * @throws IOException
	 */
	public final void renderDocument() throws IOException {
		final HTMLDocument doc = new HTMLDocument(
				folder.createFile(getFileName()), context.getOutputEncoding());
		head(doc.head());
		body(doc.body());
		doc.close();
	}

	private void head(final HTMLElement head) throws IOException {
		head.meta("Content-Type", "text/html;charset=UTF-8");
		head.link("stylesheet",
				context.getResources().getLink(folder, Resources.STYLESHEET),
				"text/css");
		head.link("shortcut icon",
				context.getResources().getLink(folder, "report.gif"),
				"image/gif");
		head.title().text(getLinkLabel());
		headExtra(head);
	}

	private void body(final HTMLElement body) throws IOException {
		body.attr("onload", getOnload());
		final HTMLElement navigation = body.div(Styles.BREADCRUMB);
		navigation.attr("id", "breadcrumb");
		infoLinks(navigation.span(Styles.RIGHT));
		breadcrumb(navigation, folder);
		body.h1().text(getLinkLabel());
		content(body);
		footer(body);
	}

	/**
	 * Hook to add extra content into the head tag.
	 * 
	 * @param head
	 *            enclosing head element
	 * @throws IOException
	 *             in case of IO problems with the report writer
	 */
	protected void headExtra(final HTMLElement head) throws IOException {
	}

	/**
	 * Returns the onload handler for this page.
	 * 
	 * @return handler or <code>null</code>
	 */
	protected String getOnload() {
		return null;
	}

	private void infoLinks(final HTMLElement span) throws IOException {
		span.a(context.getSessionsPage(), folder);
	}

	private void breadcrumb(final HTMLElement div, final ReportOutputFolder base)
			throws IOException {
		breadcrumbParent(parent, div, base);
		div.span(getLinkStyle()).text(getLinkLabel());
	}

	private static void breadcrumbParent(final ReportPage page,
			final HTMLElement div, final ReportOutputFolder base)
			throws IOException {
		if (page != null) {
			breadcrumbParent(page.parent, div, base);
			div.a(page, base);
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
	 * Creates the actual content of the page.
	 * 
	 * @param body
	 *            body tag of the page
	 * @throws IOException
	 *             in case of IO problems with the report writer
	 */
	protected abstract void content(final HTMLElement body) throws IOException;

	// === ILinkable ===

	public final String getLink(final ReportOutputFolder base) {
		return folder.getLink(base, getFileName());
	}

}
