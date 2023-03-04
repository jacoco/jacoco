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

import java.io.IOException;
import java.io.OutputStream;

import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.xml.XMLElement;

/**
 * A {@link XMLElement} with utility methods to create XHTML documents. It
 * provides methods of HTML tags to avoid magic strings in the generators.
 */
public class HTMLElement extends XMLElement {

	private static final String PUBID = "-//W3C//DTD XHTML 1.0 Strict//EN";

	private static final String SYSTEM = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";

	/**
	 * Creates a <code>html</code> root element of a XHTML document.
	 *
	 * @param encoding
	 *            character encoding used for output
	 * @param output
	 *            output stream will be closed if the root element is closed
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement(final OutputStream output, final String encoding)
			throws IOException {
		super("html", PUBID, SYSTEM, false, encoding, output);
		attr("xmlns", "http://www.w3.org/1999/xhtml");
	}

	private HTMLElement(final String name, final HTMLElement parent)
			throws IOException {
		super(name, parent);
	}

	@Override
	public HTMLElement element(final String name) throws IOException {
		return new HTMLElement(name, this);
	}

	private void classattr(final String classattr) throws IOException {
		attr("class", classattr);
	}

	/**
	 * Creates a 'head' element.
	 *
	 * @return 'head' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement head() throws IOException {
		return element("head");
	}

	/**
	 * Creates a 'body' element.
	 *
	 * @return 'body' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement body() throws IOException {
		return element("body");
	}

	/**
	 * Creates a 'meta' element.
	 *
	 * @param httpequivattr
	 *            value of the http-equiv attribute
	 * @param contentattr
	 *            value for the content attribute
	 * @return 'meta' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement meta(final String httpequivattr,
			final String contentattr) throws IOException {
		final HTMLElement meta = element("meta");
		meta.attr("http-equiv", httpequivattr);
		meta.attr("content", contentattr);
		return meta;
	}

	/**
	 * Creates a 'link' element.
	 *
	 * @param relattr
	 *            value of the rel attribute
	 * @param hrefattr
	 *            value for the href attribute
	 * @param typeattr
	 *            value for the type attribute
	 * @return 'link' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement link(final String relattr, final String hrefattr,
			final String typeattr) throws IOException {
		final HTMLElement link = element("link");
		link.attr("rel", relattr);
		link.attr("href", hrefattr);
		link.attr("type", typeattr);
		return link;
	}

	/**
	 * Creates a 'title' element.
	 *
	 * @return 'title' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement title() throws IOException {
		return element("title");
	}

	/**
	 * Creates a 'h1' element.
	 *
	 * @return 'h1' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement h1() throws IOException {
		return element("h1");
	}

	/**
	 * Creates a 'p' element.
	 *
	 * @return 'p' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement p() throws IOException {
		return element("p");
	}

	/**
	 * Creates a 'span' element.
	 *
	 * @return 'span' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement span() throws IOException {
		return element("span");
	}

	/**
	 * Creates a 'span' element.
	 *
	 * @param classattr
	 *            value of the class attribute
	 * @return 'span' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement span(final String classattr) throws IOException {
		final HTMLElement span = span();
		span.classattr(classattr);
		return span;
	}

	/**
	 * Creates a 'span' element.
	 *
	 * @param classattr
	 *            value of the class attribute
	 * @param idattr
	 *            value of the id attribute
	 * @return 'span' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement span(final String classattr, final String idattr)
			throws IOException {
		final HTMLElement span = span(classattr);
		span.attr("id", idattr);
		return span;
	}

	/**
	 * Creates a 'div' element.
	 *
	 * @param classattr
	 *            value of the class attribute
	 * @return 'div' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement div(final String classattr) throws IOException {
		final HTMLElement div = element("div");
		div.classattr(classattr);
		return div;
	}

	/**
	 * Creates a 'code' element.
	 *
	 * @return 'code' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement code() throws IOException {
		return element("code");
	}

	/**
	 * Creates a 'pre' element.
	 *
	 * @param classattr
	 *            value of the class attribute
	 * @return 'pre' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement pre(final String classattr) throws IOException {
		final HTMLElement pre = element("pre");
		pre.classattr(classattr);
		return pre;
	}

	/**
	 * Creates a 'a' element.
	 *
	 * @param hrefattr
	 *            value of the href attribute
	 * @return 'a' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement a(final String hrefattr) throws IOException {
		final HTMLElement a = element("a");
		a.attr("href", hrefattr);
		return a;
	}

	/**
	 * Creates a 'a' element.
	 *
	 * @param hrefattr
	 *            value of the href attribute
	 * @param classattr
	 *            value of the class attribute
	 * @return 'a' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement a(final String hrefattr, final String classattr)
			throws IOException {
		final HTMLElement a = a(hrefattr);
		a.classattr(classattr);
		return a;
	}

	/**
	 * Creates a link to the given {@link ILinkable}.
	 *
	 * @param linkable
	 *            object to link to
	 * @param base
	 *            base folder where the link should be placed
	 * @return 'a' element or 'span' element, if the link target does not exist
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement a(final ILinkable linkable,
			final ReportOutputFolder base) throws IOException {
		final HTMLElement a;
		final String link = linkable.getLink(base);
		if (link == null) {
			a = span(linkable.getLinkStyle());
		} else {
			a = a(link, linkable.getLinkStyle());
		}
		a.text(linkable.getLinkLabel());
		return a;
	}

	/**
	 * Creates a 'table' element.
	 *
	 * @param classattr
	 *            value of the class attribute
	 * @return 'table' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement table(final String classattr) throws IOException {
		final HTMLElement table = element("table");
		table.classattr(classattr);
		table.attr("cellspacing", "0");
		return table;
	}

	/**
	 * Creates a 'thead' element.
	 *
	 * @return 'thead' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement thead() throws IOException {
		return element("thead");
	}

	/**
	 * Creates a 'tfoot' element.
	 *
	 * @return 'tfoot' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement tfoot() throws IOException {
		return element("tfoot");
	}

	/**
	 * Creates a 'tbody' element.
	 *
	 * @return 'tbody' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement tbody() throws IOException {
		return element("tbody");
	}

	/**
	 * Creates a 'tr' element.
	 *
	 * @return 'tr' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement tr() throws IOException {
		return element("tr");
	}

	/**
	 * Creates a 'td' element.
	 *
	 * @return 'td' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement td() throws IOException {
		return element("td");
	}

	/**
	 * Creates a 'td' element.
	 *
	 * @param classattr
	 *            value of the class attribute
	 * @return 'td' element
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public HTMLElement td(final String classattr) throws IOException {
		final HTMLElement td = td();
		td.classattr(classattr);
		return td;
	}

	/**
	 * Creates a 'img' element.
	 *
	 * @param srcattr
	 *            value of the src attribute
	 * @param widthattr
	 *            value of the width attribute
	 * @param heightattr
	 *            value of the height attribute
	 * @param titleattr
	 *            value of the title and alt attribute
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public void img(final String srcattr, final int widthattr,
			final int heightattr, final String titleattr) throws IOException {
		final HTMLElement img = element("img");
		img.attr("src", srcattr);
		img.attr("width", widthattr);
		img.attr("height", heightattr);
		img.attr("title", titleattr);
		img.attr("alt", titleattr);
		img.close();
	}

	/**
	 * Creates a JavaScript 'script' element.
	 *
	 * @param srcattr
	 *            value of the src attribute
	 * @throws IOException
	 *             in case of problems with the underlying output
	 */
	public void script(final String srcattr) throws IOException {
		final HTMLElement script = element("script");
		script.attr("type", "text/javascript");
		script.attr("src", srcattr);
		// Enforce open and closing tag otherwise it won't work in browsers:
		script.text("");
		script.close();
	}

}
