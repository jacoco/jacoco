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
package org.jacoco.report.html.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.ReportOutputFolder;

/**
 * Static resource that are included with the coverage report and might be
 * referenced from created HTML pages.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class Resources {

	/** The name of the style sheet */
	public static final String STYLESHEET = "report.css";

	/** The name of the prettify style sheet */
	public static final String PRETTIFY_STYLESHEET = "prettify.css";

	/** The name of the prettify script */
	public static final String PRETTIFY_SCRIPT = "prettify.js";

	/** The name of the red part of the coverage bar */
	public static final String REDBAR = "redbar.gif";

	/** The name of the green part of the coverage bar */
	public static final String GREENBAR = "greenbar.gif";

	private final ReportOutputFolder folder;

	/**
	 * Attaches resources to the report with the given root folder.
	 * 
	 * @param root
	 *            root folder of the report
	 */
	public Resources(final ReportOutputFolder root) {
		folder = root.subFolder(".resources");
	}

	/**
	 * Returns a relative link to a static resource.
	 * 
	 * @param base
	 *            base folder from where the link should be created
	 * @param name
	 *            name of the static resource, see constants in this class
	 * @return relative link
	 */
	public String getLink(final ReportOutputFolder base, final String name) {
		return folder.getLink(base, name);
	}

	/**
	 * Determines the style sheet class for the given element type.
	 * 
	 * @param type
	 *            type of the element
	 * @return style class name
	 */
	public static String getElementStyle(final ElementType type) {
		switch (type) {
		case SESSION:
			return "el_session";
		case GROUP:
			return "el_group";
		case BUNDLE:
			return "el_bundle";
		case PACKAGE:
			return "el_package";
		case SOURCEFILE:
			return "el_source";
		case CLASS:
			return "el_class";
		case METHOD:
			return "el_method";
		}
		throw new IllegalArgumentException("Unknown element type: " + type);
	}

	/**
	 * Copies all static resources into the report.
	 * 
	 * @throws IOException
	 *             if the resources can't be written to the report
	 */
	public void copyResources() throws IOException {
		copyResource(STYLESHEET);
		copyResource("session.gif");
		copyResource("group.gif");
		copyResource("bundle.gif");
		copyResource("package.gif");
		copyResource("source.gif");
		copyResource("class.gif");
		copyResource("method.gif");
		copyResource(REDBAR);
		copyResource(GREENBAR);
		copyResource(PRETTIFY_STYLESHEET);
		copyResource(PRETTIFY_SCRIPT);
	}

	private void copyResource(final String name) throws IOException {
		final InputStream in = Resources.class.getResourceAsStream(name);
		final OutputStream out = folder.createFile(name);
		final byte[] buffer = new byte[256];
		int len;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		in.close();
		out.close();
	}

}
