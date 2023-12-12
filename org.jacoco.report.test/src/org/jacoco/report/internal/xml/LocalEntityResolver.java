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
package org.jacoco.report.internal.xml;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Loader for local DTD definitions to avoid network access.
 */
class LocalEntityResolver implements EntityResolver {

	private final Class<?> resourceDelegate;

	public LocalEntityResolver(Class<?> resourceDelegate) {
		this.resourceDelegate = resourceDelegate;
	}

	public InputSource resolveEntity(final String publicId, String systemId)
			throws SAXException, IOException {
		final int sep = systemId.lastIndexOf('/');
		if (sep != -1) {
			systemId = systemId.substring(sep + 1);
		}
		final InputStream in = resourceDelegate.getResourceAsStream(systemId);
		if (in == null) {
			throw new IOException("No local copy for " + systemId);
		}
		return new InputSource(in);
	}

}
