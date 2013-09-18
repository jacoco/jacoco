/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Stub {@link ISourceFileLocator} for tests.
 */
public class StubLocator implements ISourceFileLocator {

	private final Map<String, Reader> sources = new HashMap<String, Reader>();

	public void put(String path, String content) {
		sources.put(path, new StringReader(content));
	}

	public Reader getSourceFile(String packageName, String fileName)
			throws IOException {
		return sources.get(packageName + "/" + fileName);
	}

	public int getTabWidth() {
		return 0;
	}
}