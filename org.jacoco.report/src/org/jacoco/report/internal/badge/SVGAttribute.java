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

import java.io.IOException;

/**
 * As SVG makes heavy use of XML attributes, these can be specified with static
 * factory methods from this class. This allows named parameter like
 * definitions, e.g:
 * 
 * <pre>
 * g.rect(x(8), y(10), width(20), height(12));
 * </pre>
 */
class SVGAttribute {

	private final String name;
	private final String value;

	private SVGAttribute(final String name, final String value) {
		this.name = name;
		this.value = value;
	}

	private SVGAttribute(final String name, final int value) {
		this(name, String.valueOf(value));
	}

	private SVGAttribute(final String name, final double value) {
		this(name, String.valueOf(value));
	}

	void apply(final SVGElement element) throws IOException {
		element.attr(name, value);
	}

	static SVGAttribute x(final int x) {
		return new SVGAttribute("x", x);
	}

	static SVGAttribute y(final int y) {
		return new SVGAttribute("y", y);
	}

	static SVGAttribute x2(final int x) {
		return new SVGAttribute("x2", x);
	}

	static SVGAttribute y2(final String y) {
		return new SVGAttribute("y2", y);
	}

	static SVGAttribute width(final int width) {
		return new SVGAttribute("width", width);
	}

	static SVGAttribute height(final int height) {
		return new SVGAttribute("height", height);
	}

	static SVGAttribute offset(final int offset) {
		return new SVGAttribute("offset", offset);
	}

	static SVGAttribute stopOpacity(final double opacity) {
		return new SVGAttribute("stop-opacity", opacity);
	}

	static SVGAttribute fillOpacity(final double opacity) {
		return new SVGAttribute("fill-opacity", opacity);
	}

	static SVGAttribute stopColor(final String color) {
		return new SVGAttribute("stop-color", color);
	}

	static SVGAttribute rx(final int rx) {
		return new SVGAttribute("rx", rx);
	}

	static SVGAttribute id(final String id) {
		return new SVGAttribute("id", id);
	}

	static SVGAttribute fill(final String fill) {
		return new SVGAttribute("fill", fill);
	}

	static SVGAttribute clipPath(final String path) {
		return new SVGAttribute("clip-path", path);
	}

	static SVGAttribute textAnchor(final String anchor) {
		return new SVGAttribute("text-anchor", anchor);
	}

	static SVGAttribute fontFamily(final String family) {
		return new SVGAttribute("font-family", family);
	}

	static SVGAttribute fontSize(final int size) {
		return new SVGAttribute("font-size", size);
	}

}
