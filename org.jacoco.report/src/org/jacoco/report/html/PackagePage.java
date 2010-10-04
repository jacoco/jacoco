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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jacoco.core.analysis.ClassCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.core.analysis.SourceFileCoverage;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.ReportOutputFolder;
import org.jacoco.report.html.resources.Resources;

/**
 * Page showing coverage information for a Java package. The page contains a
 * table with all classes of the package.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class PackagePage extends NodePage {

	private final List<ClassPage> classes = new ArrayList<ClassPage>();

	private final Map<String, SourceFilePage> sourceFiles = new HashMap<String, SourceFilePage>();

	/**
	 * Creates a new visitor in the given context.
	 * 
	 * @param node
	 * @param parent
	 * @param folder
	 * @param context
	 */
	public PackagePage(final ICoverageNode node, final ReportPage parent,
			final ReportOutputFolder folder, final IHTMLReportContext context) {
		super(node, parent, folder, context);
	}

	public IReportVisitor visitChild(final ICoverageNode node) {
		final ElementType type = node.getElementType();
		switch (type) {
		case SOURCEFILE:
			final SourceFilePage sourcePage = new SourceFilePage(
					(SourceFileCoverage) node, this, folder, context);
			sourceFiles.put(node.getName(), sourcePage);
			return sourcePage;
		case CLASS:
			final ClassPage classPage = new ClassPage((ClassCoverage) node,
					this, sourceFiles, folder, context);
			classes.add(classPage);
			return classPage;
		}
		throw new IllegalStateException("Unexpected element type " + type);
	}

	@Override
	public void visitEnd(final ISourceFileLocator sourceFileLocator)
			throws IOException {
		super.visitEnd(sourceFileLocator);
		// free memory, otherwise we will keep the complete tree:
		classes.clear();
		sourceFiles.clear();
	}

	@Override
	protected void headExtra(final HTMLElement head) throws IOException {
		super.headExtra(head);
		head.script("text/javascript",
				context.getResources().getLink(folder, Resources.SORT_SCRIPT));
	}

	@Override
	protected String getOnload() {
		return "initialSort()";
	}

	@Override
	protected void content(final HTMLElement body) throws IOException {
		context.getTable().render(body, classes, getNode(),
				context.getResources(), folder);
	}

	@Override
	protected String getFileName() {
		return "index.html";
	}

	@Override
	public String getLinkLabel() {
		return context.getLanguageNames().getPackageName(getNode().getName());
	}

}
