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
 *  Analysis and instrumentation of Java class files for code coverage. The main
 *  entry points are:
 * </p>
 *
 * <ul>
 *   <li>{@link org.jacoco.core.analysis.Analyzer}: Class structure information for
 *   analysis and report generation.</li>
 *   <li>{@link org.jacoco.core.instr.Instrumenter}: Classes instrumentation for
 *   tracing coverage at runtime.</li>
 * </ul>
 */
package org.jacoco.core.instr;