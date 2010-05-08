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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.MemoryMultiReportOutput;
import org.jacoco.report.ReportOutputFolder;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Resources}.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class ResourcesTest {

	private MemoryMultiReportOutput output;

	private ReportOutputFolder root;

	private Resources resources;

	@Before
	public void setup() {
		output = new MemoryMultiReportOutput();
		root = new ReportOutputFolder(output);
		resources = new Resources(root);
	}

	@Test
	public void testGetLink() {
		ReportOutputFolder base = root.subFolder("f1").subFolder("f2");
		assertEquals("../../.resources/test.png", resources.getLink(base,
				"test.png"));

	}

	@Test
	public void testCopyResources() throws IOException {
		resources.copyResources();
		output.assertFile(".resources/report.css");
		output.assertFile(".resources/prettify.css");
		output.assertFile(".resources/prettify.js");
		output.assertFile(".resources/session.gif");
		output.assertFile(".resources/report.gif");
		output.assertFile(".resources/group.gif");
		output.assertFile(".resources/bundle.gif");
		output.assertFile(".resources/package.gif");
		output.assertFile(".resources/source.gif");
		output.assertFile(".resources/class.gif");
		output.assertFile(".resources/method.gif");
		output.assertFile(".resources/greenbar.gif");
		output.assertFile(".resources/redbar.gif");
	}

	@Test
	public void testGetElementStyle() {
		assertEquals("el_bundle", Resources.getElementStyle(ElementType.BUNDLE));
		assertEquals("el_package", Resources
				.getElementStyle(ElementType.PACKAGE));
		assertEquals("el_source", Resources
				.getElementStyle(ElementType.SOURCEFILE));
		assertEquals("el_class", Resources.getElementStyle(ElementType.CLASS));
		assertEquals("el_method", Resources.getElementStyle(ElementType.METHOD));
	}

}
