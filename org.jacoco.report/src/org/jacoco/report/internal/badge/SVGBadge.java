/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.internal.badge;

import static org.jacoco.report.internal.badge.SVGAttribute.clipPath;
import static org.jacoco.report.internal.badge.SVGAttribute.fill;
import static org.jacoco.report.internal.badge.SVGAttribute.fillOpacity;
import static org.jacoco.report.internal.badge.SVGAttribute.fontFamily;
import static org.jacoco.report.internal.badge.SVGAttribute.fontSize;
import static org.jacoco.report.internal.badge.SVGAttribute.height;
import static org.jacoco.report.internal.badge.SVGAttribute.id;
import static org.jacoco.report.internal.badge.SVGAttribute.offset;
import static org.jacoco.report.internal.badge.SVGAttribute.rx;
import static org.jacoco.report.internal.badge.SVGAttribute.stopColor;
import static org.jacoco.report.internal.badge.SVGAttribute.stopOpacity;
import static org.jacoco.report.internal.badge.SVGAttribute.textAnchor;
import static org.jacoco.report.internal.badge.SVGAttribute.width;
import static org.jacoco.report.internal.badge.SVGAttribute.x;
import static org.jacoco.report.internal.badge.SVGAttribute.x2;
import static org.jacoco.report.internal.badge.SVGAttribute.y;
import static org.jacoco.report.internal.badge.SVGAttribute.y2;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Simple badge with two separate text blocks.
 */
public class SVGBadge {

	private static final int HEIGHT = 20;

	private static final String ID_BUTTON = "button";
	private static final String ID_LIGHT = "light";

	private final int width1;
	private final String text1;
	private final int width2;
	private final String text2;

	/**
	 * Initialize a badge with the given parameters.
	 * 
	 * @param width1
	 *            width of the left part
	 * @param text1
	 *            text shown in the left part
	 * @param width2
	 *            width of the right part
	 * @param text2
	 *            text shown in the right part
	 */
	public SVGBadge(final int width1, final String text1, final int width2,
			final String text2) {
		this.width1 = width1;
		this.text1 = text1;
		this.width2 = width2;
		this.text2 = text2;
	}

	/**
	 * Renders the SVG document with the badge.
	 * 
	 * @param out
	 *            output to write the SVG content to
	 * @throws IOException
	 *             in case of problems with the output stream
	 */
	public void render(final OutputStream out) throws IOException {
		final SVGElement svg = new SVGElement(out, width(width1 + width2),
				height(HEIGHT));
		buttonClipPath(svg);
		lightGradient(svg);
		background(svg);
		final SVGElement textParent = svg.g(textAnchor("middle"),
				fontFamily("DejaVu Sans,Verdana,Geneva,sans-serif"),
				fontSize(11));
		textWithShadow(textParent, width1 / 2, text1);
		textWithShadow(textParent, width1 + width2 / 2, text2);
		svg.close();
	}

	private void buttonClipPath(final SVGElement parent) throws IOException {
		final SVGElement clipPath = parent.clipPath(id(ID_BUTTON));
		clipPath.rect(width(width1 + width2), height(HEIGHT), rx(3),
				fill("#fff"));
	}

	private void lightGradient(final SVGElement parent) throws IOException {
		final SVGElement gradient = parent.linearGradient(id(ID_LIGHT), x2(0),
				y2("100%"));
		gradient.stop(offset(0), stopOpacity(0.1), stopColor("#bbb"));
		gradient.stop(offset(1), stopOpacity(0.1));
	}

	private void background(final SVGElement parent) throws IOException {
		final SVGElement g = parent.g(clipPath(ref(ID_BUTTON)));
		g.rect(width(width1), height(HEIGHT), fill("#555"));
		g.rect(x(width1), width(width2), height(HEIGHT), fill("#4c1"));
		g.rect(width(width1 + width2), height(HEIGHT), fill(ref(ID_LIGHT)));
	}

	private void textWithShadow(final SVGElement parent, final int x,
			final String text) throws IOException {
		parent.text(x(x), y(15), fill("#000"), fillOpacity(0.3)).text(text);
		parent.text(x(x), y(14), fill("#fff")).text(text);
	}

	private String ref(final String id) {
		return String.format("url(#%s)", id);
	}

}
