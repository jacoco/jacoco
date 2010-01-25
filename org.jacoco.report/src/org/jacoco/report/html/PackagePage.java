/*******************************************************************************
 * Copyright (c) 2009 Mountainminds GmbH & Co. KG and others
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
import org.jacoco.core.analysis.SourceFileCoverage;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.ReportOutputFolder;

/**
 * Page showing coverage information for a Java package. The page contains a
 * table with all classes of the package.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: $
 */
public class PackagePage extends ReportPage {

	private final List<ClassPage> classes = new ArrayList<ClassPage>();

	private final Map<String, SourceFilePage> sourceFiles = new HashMap<String, SourceFilePage>();

	/**
	 * Creates a new visitor in the given context.
	 * 
	 * @param node
	 * @param parent
	 * @param outputFolder
	 * @param context
	 */
	public PackagePage(final ICoverageNode node, final ReportPage parent,
			final ReportOutputFolder outputFolder,
			final IHTMLReportContext context) {
		super(node, parent, outputFolder, context);
	}

	public IReportVisitor visitChild(final ICoverageNode node) {
		final ElementType type = node.getElementType();
		switch (type) {
		case SOURCEFILE:
			final SourceFilePage sourcePage = new SourceFilePage(
					(SourceFileCoverage) node, this, outputFolder, context);
			sourceFiles.put(node.getName(), sourcePage);
			return sourcePage;
		case CLASS:
			final ClassPage classPage = new ClassPage((ClassCoverage) node,
					this, sourceFiles, outputFolder, context);
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
	protected void content(final HTMLElement body,
			final ISourceFileLocator sourceFileLocator) throws IOException {
		context.getTable(getNode().getElementType()).render(body, classes,
				getNode(), context.getResources(), outputFolder);
	}

	@Override
	protected String getFileName() {
		return "index.html";
	}

	@Override
	public String getLabel() {
		return context.getLanguageNames().getPackageName(getNode().getName());
	}

	@Override
	protected ReportOutputFolder getFolder(final ReportOutputFolder base) {
		return base.subFolder(getLabel());
	}

}
