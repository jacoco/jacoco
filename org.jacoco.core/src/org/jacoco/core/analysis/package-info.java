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
 *******************************************************************************/

/**
 * <p>
 * Structures to represent coverage information in node hierarchies. Each node
 * represents a Java element like class or method. The coverage information is
 * build from {@linkplain org.jacoco.core.data.IStructureVisitor structure}
 * and {@linkplain org.jacoco.core.data.IExecutionDataVisitor execution}</a>
 * information.
 * </p>
 *
 * <p>
 * A coverage analysis is represented in the following node hierarchy:
 * </p>
 *
 * <pre>
 * +-- {@linkplain org.jacoco.core.analysis.ICoverageNode.ElementType#GROUP Group} (optional)
 *     +-- {@linkplain org.jacoco.core.analysis.ICoverageNode.ElementType#BUNDLE Bundle}
 *         +-- {@linkplain org.jacoco.core.analysis.ICoverageNode.ElementType#PACKAGE Package}
 *             +-- {@linkplain org.jacoco.core.analysis.ICoverageNode.ElementType#SOURCEFILE Source File}
 *                 +-- {@linkplain org.jacoco.core.analysis.ICoverageNode.ElementType#CLASS Class}
 *                     +-- {@linkplain org.jacoco.core.analysis.ICoverageNode.ElementType#METHOD Method}
 * </pre>
 */
package org.jacoco.core.analysis;