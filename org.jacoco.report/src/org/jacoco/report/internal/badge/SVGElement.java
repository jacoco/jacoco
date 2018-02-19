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
import java.io.OutputStream;

import org.jacoco.report.internal.xml.XMLElement;

/**
 * A {@link XMLElement} with utility methods for SVG tags.
 */
class SVGElement extends XMLElement {

	public SVGElement(final OutputStream output,
			final SVGAttribute... attributes) throws IOException {
		super("svg", null, null, false, "UTF-8", output);
		attr("xmlns", "http://www.w3.org/2000/svg");
		for (final SVGAttribute a : attributes) {
			a.apply(this);
		}
	}

	private SVGElement(final String name, final XMLElement parent)
			throws IOException {
		super(name, parent);
	}

	@Override
	public SVGElement element(final String name) throws IOException {
		return new SVGElement(name, this);
	}

	private SVGElement element(final String name,
			final SVGAttribute... attributes) throws IOException {
		final SVGElement element = element(name);
		for (final SVGAttribute a : attributes) {
			a.apply(element);
		}
		return element;
	}

	SVGElement clipPath(final SVGAttribute... attributes) throws IOException {
		return element("clipPath", attributes);
	}

	SVGElement linearGradient(final SVGAttribute... attributes)
			throws IOException {
		return element("linearGradient", attributes);
	}

	SVGElement stop(final SVGAttribute... attributes) throws IOException {
		return element("stop", attributes);
	}

	SVGElement g(final SVGAttribute... attributes) throws IOException {
		return element("g", attributes);
	}

	SVGElement rect(final SVGAttribute... attributes) throws IOException {
		return element("rect", attributes);
	}

	SVGElement text(final SVGAttribute... attributes) throws IOException {
		return element("text", attributes);
	}

}
